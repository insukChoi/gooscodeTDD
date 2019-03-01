package auctionsniper.endtoend;

import auctionsniper.Main;
import auctionsniper.SniperState;
import auctionsniper.unit.ui.MainWindow;
import auctionsniper.unit.ui.SnipersTableModel;
import org.hamcrest.Matchers;

import java.io.IOException;

import static auctionsniper.endtoend.FakeAuctionServer.XMPP_HOSTNAME;

public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper4";
    public static final String SNIPER_PASSWORD = "sniper4";
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@" + XMPP_HOSTNAME + "/Auction";

    private AuctionSniperDriver driver;
    private AuctionLogDriver logDriver = new AuctionLogDriver();

    public void startBiddingIn(final FakeAuctionServer... auctions) throws Exception {
        startSniper();
        for (FakeAuctionServer auction : auctions) {
            driver.startBiddingFor(auction.getItemId(), Integer.MAX_VALUE);
            driver.showsSniperStatus(auction.getItemId(), 0, 0, SnipersTableModel.textFor(SniperState.JOINING));
        }
    }

    public void startBiddingWithStopPrice(FakeAuctionServer auction, int stopPrice) throws Exception {
        startSniper();
        driver.startBiddingFor(auction.getItemId(), stopPrice);
        driver.showsSniperStatus(auction.getItemId(), 0, 0, SnipersTableModel.textFor(SniperState.JOINING));
    }

    private void startSniper() throws Exception {
        logDriver.clearLog();

        Main.main(FakeAuctionServer.XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD);
        driver = new AuctionSniperDriver(3000);
        driver.hasTitle(MainWindow.APPLICATION_TITLE);
        driver.hasColumnTitle();
    }

    public void stop() {
        if (driver != null) {
            driver.dispose();
        }
    }

    public void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, SnipersTableModel.textFor(SniperState.BIDDING));
    }

    public void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
        driver.showsSniperStatus(auction.getItemId(), winningBid, winningBid, SnipersTableModel.textFor(SniperState.WINNING));
    }

    public void hasShownSniperIsWonAuction(FakeAuctionServer auction, int lastPrice) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastPrice, SnipersTableModel.textFor(SniperState.WON));
    }

    public void hasShownSniperIsLostAuction(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, SnipersTableModel.textFor(SniperState.LOST));
    }

    public void hasShownSniperIsLosing(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemId(), lastPrice, lastBid, SnipersTableModel.textFor(SniperState.LOSING));
    }

    public void hasShownSniperIsFailed(FakeAuctionServer auction) {
        driver.showsSniperStatus(auction.getItemId(), 0, 0, SnipersTableModel.textFor(SniperState.FAILED));
    }

    public void reportsInvalidMessage(FakeAuctionServer auction, String message) throws IOException {
        logDriver.hasEntry(Matchers.containsString(message));
    }
}
