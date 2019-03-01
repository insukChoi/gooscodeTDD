package auctionsniper;

import java.util.EventListener;

public interface AuctionEventListener extends EventListener {
    enum PriceSource {
        FromSniper, FromOtherBidder
    }

    void auctionFailed();
    void auctionClosed();
    void currentPrice(int price, int increment, PriceSource from);
}
