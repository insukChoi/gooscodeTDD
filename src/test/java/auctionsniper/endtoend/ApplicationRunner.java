package auctionsniper.endtoend;

import auctionsniper.Main;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.ui.MainWindow;
import auctionsniper.ui.SnipersTableModel;

import static auctionsniper.endtoend.FakeAuctionServer.XMPP_HOSTNAME;
import static auctionsniper.ui.SnipersTableModel.textFor;

public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID = "sniper@antop.org/Auction";

    private AuctionSniperDriver driver;

    private String itemId;

    public void  startBiddingIn(final FakeAuctionServer auction) {
        itemId = auction.getItemId();

        Thread thread = new Thread("Test Application"){
            @Override
            public void run(){
                try {
                    Main.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD, auction.getItemId());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

        driver = new AuctionSniperDriver(1000);
        driver.hasTitle(MainWindow.APPLICATION_TITLE);
        driver.hasColumnTitle();
    }

    public void hasShownSniperIsBidding(int lastPrice, int lastBid){
        driver.showsSniperStatus(itemId, lastPrice, lastBid, textFor(SniperState.BIDDING));
    }

    public void showsSniperHasLostAuction(int lastPrice, int lastBid){
        driver.showsSniperStatus(itemId, lastPrice, lastBid, textFor(SniperState.LOST));
    }

    public void hasShownSniperIsWinning(int winningBid) {
        driver.showsSniperStatus(itemId, winningBid, winningBid, textFor(SniperState.WINNING));
    }

    public void showsSniperHasWonAuction(int lastPrice) {
        driver.showsSniperStatus(itemId, lastPrice, lastPrice, textFor(SniperState.WON));
    }


    public void stop(){
        if (driver != null){
            driver.dispose();
        }
    }
}
