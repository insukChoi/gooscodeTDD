package endtoend;

import org.junit.After;
import org.junit.Test;

public class AutionSniperEndToEndTest {

    private final FakeAuctionServer auction = new FakeAuctionServer("item-54321");
    private final ApplicationRunner application = new ApplicationRunner();

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        auction.startSellingItem();
        application.startBiddingIn(auction);
        auction.hasReceivedJoinRequestFromSniper();
        auction.announceClosed();
        application.showSniperHasLostAuction();
    }

    // 추가 정리 루틴
    @After
    public void stopAuction(){
        auction.stop();
    }
    @After
    public void stopApplication(){
        application.stop();
    }
}
