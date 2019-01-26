package auctionsniper.unit;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.AuctionSniper;
import auctionsniper.SniperListener;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionSniperTest {
    private enum SniperState { idle, winning, bidding }
    private SniperState sniperState = SniperState.idle;
    private class SniperListenerStub implements SniperListener {
        @Override
        public void sniperBidding() {
            sniperState = SniperState.bidding;
        }

        @Override
        public void sniperWinning() {
            sniperState = SniperState.winning;
        }

        @Override
        public void sniperWon() {}

        @Override
        public void sniperLost() {}

    }

    private Auction auction = mock(Auction.class);
    private final SniperListener sniperListener = spy(new SniperListenerStub());
    private final AuctionSniper sniper = new AuctionSniper(auction, sniperListener);

    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperLost();
    }

    @Test
    public void reportsLostWhenAuctionClosesWhenBidding() {
        sniper.currentPrice(123, 45, AuctionEventListener.PriceSource.FromOtherBidder);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperLost();
        assertEquals(SniperState.bidding, sniperState);
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        sniper.currentPrice(123, 45, AuctionEventListener.PriceSource.FromSniper);
        sniper.auctionClosed();

        verify(sniperListener, atLeastOnce()).sniperWon();
        assertEquals(SniperState.winning, sniperState);
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;

        sniper.currentPrice(price, increment, AuctionEventListener.PriceSource.FromOtherBidder);

        verify(auction, times(1)).bid(price + increment);
        verify(sniperListener, atLeastOnce()).sniperBidding();
    }

}
