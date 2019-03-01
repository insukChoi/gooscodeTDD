package auctionsniper.unit.util;

import auctionsniper.AuctionEventListener;
import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.EventListener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AnnouncerTest {

    @Test
    void addEventListeners() {
        Announcer<AuctionEventListener> announcer = Announcer.to(AuctionEventListener.class);

        AuctionEventListener listener1 = mock(AuctionEventListener.class);
        AuctionEventListener listener2 = mock(AuctionEventListener.class);

        announcer.addListener(listener1);
        announcer.addListener(listener2);

        announcer.announce().auctionClosed();

        verify(listener1, times(1)).auctionClosed();
        verify(listener2, times(1)).auctionClosed();
    }

    @Test
    void removeEventListener() {
        Announcer<SniperListener> announcer = Announcer.to(SniperListener.class);

        SniperListener listener1 = mock(SniperListener.class);
        SniperListener listener2 = mock(SniperListener.class);

        announcer.addListener(listener1);
        announcer.addListener(listener2);

        announcer.removeListener(listener1);

        announcer.announce().sniperStateChanged(SniperSnapshot.joining("item"));

        verify(listener1, never()).sniperStateChanged(any());
        verify(listener2, times(1)).sniperStateChanged(any(SniperSnapshot.class));
    }

    @Test
    public void errorWhenUsingNonPublicInterface() {
        Announcer<ParentListener> announcer = Announcer.to(ParentListener.class);
        announcer.addListener(Mockito.mock(ParentListener.class));
        // action and verify
        //assertThrows(IllegalArgumentException.class, () -> announcer.announce().someMethod());
    }

    // not public
    interface ParentListener extends EventListener {
        void someMethod();
    }

    @Test
    public void errorWhenThrowsExceptionInvokeMethod() {
        // mock
        AuctionEventListener auctionEventListener = mock(AuctionEventListener.class);
        // currentPrice() 호출하면 예외 발생
        doAnswer(invocation -> {
            AuctionEventListener.PriceSource priceSource = invocation.getArgument(2);
            if (priceSource == AuctionEventListener.PriceSource.FromSniper) {
                // java.lang.RuntimeException 발생
                throw new RuntimeException("throw RuntimeException when PriceSource is sniper.");
            } else {
                // java.lang.Error 발생
                throw new Error("throw Error when PriceSource is other bidder.");
            }
        }).when(auctionEventListener).currentPrice(anyInt(), anyInt(), any(AuctionEventListener.PriceSource.class));

        // currentPrice() 호출하면 java.lang.Exception 발생
        doAnswer(invocation -> {
            throw new Exception("throw exception when call currentPrice(...)");
        }).when(auctionEventListener).auctionClosed();

        // preparations
        Announcer<AuctionEventListener> announcer = Announcer.to(AuctionEventListener.class);
        announcer.addListener(auctionEventListener);

        // action and verify #1
        assertThrows(RuntimeException.class, () -> announcer.announce().currentPrice(10, 1, AuctionEventListener.PriceSource.FromSniper));
        // action and verify #2
        assertThrows(Error.class, () -> announcer.announce().currentPrice(10, 1, AuctionEventListener.PriceSource.FromOtherBidder));
        // action and verify #3
        assertThrows(Exception.class, () -> announcer.announce().auctionClosed());
    }

}
