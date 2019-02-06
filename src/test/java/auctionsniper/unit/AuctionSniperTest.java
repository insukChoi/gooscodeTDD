package auctionsniper.unit;

import auctionsniper.*;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static auctionsniper.AuctionEventListener.PriceSource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class AuctionSniperTest {

    private final String ITEM_ID = "test-item";
    private final Auction auction = Mockito.mock(Auction.class);
    private final SniperListener sniperListener = Mockito.mock(SniperListener.class);
    private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction, sniperListener);
    private final ArgumentCaptor<SniperSnapshot> argument = ArgumentCaptor.forClass(SniperSnapshot.class);

    @Test
    void reportsLostWhenAuctionClosesImmediately() {
        sniper.auctionClosed();
        verify(sniperListener).sniperStateChanged(argument.capture());
        assertEquals(SniperState.LOST, argument.getValue().state);
    }

    @Test
    void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;
        // action #1
        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder);

        verify(auction).bid(price + increment);
        verify(sniperListener).sniperStateChanged(refEq(new SniperSnapshot(ITEM_ID, price, bid, SniperState.BIDDING)));
    }

    @Test
    void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, PriceSource.FromSniper);

        verify(sniperListener, times(2)).sniperStateChanged(argument.capture());
        assertEquals(SniperState.WINNING, argument.getValue().state);
    }

    @Test
    void reportsLostIfAuctionClosesWhenBidding() {
        // 다른 입찰자가 입찰
        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder);
        verify(sniperListener).sniperStateChanged(refEq(new SniperSnapshot(ITEM_ID, 123, 123 + 45, SniperState.BIDDING)));
        // 경매 종료
        sniper.auctionClosed();
        // verify
        verify(sniperListener, atLeastOnce()).sniperStateChanged(argument.capture());
        assertEquals(SniperState.LOST, argument.getValue().state);
    }

    @Test
    void reportsWonIfAuctionClosesWhenWinning() {
        // 스나이퍼가 입찰
        sniper.currentPrice(123, 45, PriceSource.FromSniper);
        // verify: [낙찰중] 상태 확인
        verify(sniperListener).sniperStateChanged(argument.capture());
        assertEquals(SniperState.WINNING, argument.getValue().state);
        // 경매 종료
        sniper.auctionClosed();
        // 낙찰
        verify(sniperListener, atLeastOnce()).sniperStateChanged(argument.capture());
        assertEquals(SniperState.WON, argument.getValue().state);
    }


}