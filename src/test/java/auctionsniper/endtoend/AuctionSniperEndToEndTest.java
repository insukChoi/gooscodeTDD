package auctionsniper.endtoend;

import org.junit.After;
import org.junit.Test;

public class AuctionSniperEndToEndTest {
    static {
        // WARNING: could not load keyboard layout KR, using fallback layout with reduced capabilities
        // java.lang.IllegalArgumentException: no stroke available for character '-
        // https://stackoverflow.com/questions/23316432/windowlicker-is-not-working-on-os-x
        System.setProperty("com.objogate.wl.keyboard", "US");
    }

    private final FakeAuctionServer auction = new FakeAuctionServer("item-34567");
    private final FakeAuctionServer auction2 = new FakeAuctionServer("item-45678");
    private final ApplicationRunner application = new ApplicationRunner();

    @Test
    public void sniperJoinsAuctionUntilAuctionCloses() throws Exception {
        // 1단계: [서버] 품목 판매 시작
        auction.startSellingItem();
        // 2단계: [어플] 입찰 시작
        application.startBiddingIn(auction);
        // 3단계: [서버] 스나이퍼로부터 가입 요청을 받았는지 확인
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
        // 4단계: [서버] 경매 폐쇠 알림
        auction.announceClosed();
        // 5단계: [어플] 경매가 낙찰 되었는지 확인
        application.hasShownSniperIsLostAuction(auction,0, 0);
    }

    @Test
    public void sniperMakesAHigherBidButLoses() throws Exception {
        // 1단계: [서버] 품목 판매 시작
        auction.startSellingItem();
        // 2단계: [어플] 입찰 시작
        application.startBiddingIn(auction);
        // 3단계: [서버] 스나이퍼로부터 가입 요청을 받았는지 확인
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
        // 4단계: [서버] 품목가격이 1000 이고, 다음 입찰에 대한 증가액이 98 이며, 낙찰자는 "다른 입찰자" 라는 소식을 전달한다.
        auction.reportPrice(1000, 98, "other bidder");
        // 5단계: [어플] 스나이퍼가 경매로부터 가격 갱신 메시지를 받고 난 후 입찰하고 있는지 확인 요청
        application.hasShownSniperIsBidding(auction,1000, 1098);
        // 6단계: [서버] 스나이퍼로부터 입찰을 받았고 해당 입찰이 마지막 가격에 최소 증가액을 더한 것과 가격이 같은지 확인
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);
        // 7단계: [서버] 여전이 스나이퍼가 경매에서 낙찰을 못했으므로 경매를 종료함
        auction.announceClosed();
        application.hasShownSniperIsLostAuction(auction,1000, 1098);

    }

    @Test
    public void sniperWinsAnAuctionByBiddingHigher() throws Exception {
        // 1단계: [서버] 품목 판매 시작
        auction.startSellingItem();

        // 2단계: [어플] 입찰 시작
        application.startBiddingIn(auction);
        // 3단계: [서버] 스나이퍼로부터 가입 요청을 받았는지 확인
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

        // 4단계: [서버]
        auction.reportPrice(1000, 98, "other bidder");
        // 5단계: [어플] 스나이퍼가 경매로부터 가격 갱신 메시지를 받고 난 후 입찰하고 있는지 확인해달라고 서버에 요청
        application.hasShownSniperIsBidding(auction,1000, 1098);

        // 6단계: [서버] 스나이퍼로부터 입찰을 받았고 해당 입찰이 마지막 가격에 최소 증가액을 더한 것과 가격이 같은지 확인
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1098,97, ApplicationRunner.SNIPER_XMPP_ID);
        application.hasShownSniperIsWinning(auction,1098);

        // 7단계: [서버] 여전이 스나이퍼가 경매에서 낙찰을 못했으므로 경매를 종료함
        auction.announceClosed();
        application.hasShownSniperIsWonAuction(auction,1098);
    }

    @Test
    public void sniperBidsForMultipleItems() throws Exception {
        // 1단계: [서버] 여러 품목 판매 시작
        auction.startSellingItem();
        auction2.startSellingItem();

        // 2단계: [어플] 여러 품목 입찰 시작
        application.startBiddingIn(auction, auction2);
        // 3단계: [서버] 스나이퍼로부터 가입 요청을 받았는지 확인
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
        auction2.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

        // 4단계: [서버]
        auction.reportPrice(1000, 98, "other bidder");
        // 5단계: [서버] 스나이퍼로부터 입찰을 받았고 해당 입찰이 마지막 가격에 최소 증가액을 더한 것과 가격이 같은지 확인
        auction.hasReceivedBid(1098, ApplicationRunner.SNIPER_XMPP_ID);

        auction2.reportPrice(500, 21, "other bidder");
        auction2.hasReceivedBid(521, ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_XMPP_ID);
        auction2.reportPrice(521, 22, ApplicationRunner.SNIPER_XMPP_ID);
        // 6단계: [어플] 낙찰
        application.hasShownSniperIsWinning(auction, 1098);
        application.hasShownSniperIsWinning(auction2, 521);

        // 7단계: [서버] 종료
        auction.announceClosed();
        auction2.announceClosed();

    }

    @Test
    public void sniperLosesAnAuctionWhenThePriceIsTooHigh() throws Exception {
        auction.startSellingItem();
        application.startBiddingWithStopPrice(auction, 1100);
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
        auction.reportPrice(1000, 98, "other bidder"); // 1st bid other bidder

        application.hasShownSniperIsBidding(auction, 1000, 1098); // 2nd bid by sniper

        auction.reportPrice(1197, 10, "John"); // 3st bid by John
        application.hasShownSniperIsLosing(auction, 1197, 1098);

        auction.reportPrice(1207, 10, "Antop"); // 4st bid by Antop
        application.hasShownSniperIsLosing(auction, 1207, 1098);

        auction.announceClosed();
        application.hasShownSniperIsLostAuction(auction, 1207, 1098);
    }

    @Test
    public void sniperReportsInvalidAuctionMessageAndStopsRespondingToEvents() throws Exception {
        String brokenMessage = "a broken message";
        auction.startSellingItem();
        auction2.startSellingItem();

        application.startBiddingIn(auction, auction2);
        auction.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);

        auction.reportPrice(500, 20, "other bidder");
        auction.hasReceivedBid(520, ApplicationRunner.SNIPER_XMPP_ID);
        // 잘못된 메세지 전송
        auction.sendInvalidMessageContaining(brokenMessage);
        // Failed 상태 확인
        application.hasShownSniperIsFailed(auction);

        // 다른 입찰자가 해당 아이템을 입찰함.
        // ※ auction1 스나이퍼는 Failed 상태이기 때문에 더이상 진행을 하면 안된다.
        auction.reportPrice(520, 21, "other bidder");
        // p249 뭔가 일어나지 않음을 테스트하기
        waitForAnotherAuctionEvent();
        application.reportsInvalidMessage(auction, brokenMessage);
        // 여전히 auction1은 Failed 인가?
        application.hasShownSniperIsFailed(auction);
    }

    private void waitForAnotherAuctionEvent() throws Exception {
        auction2.hasReceivedJoinRequestFromSniper(ApplicationRunner.SNIPER_XMPP_ID);
        auction2.reportPrice(600, 6, "other bidder");
        application.hasShownSniperIsBidding(auction2, 600, 606);
    }

    // 추가 정리 루틴
    @After
    public void stopAuction(){
        auction.stop();
        auction2.stop();
    }
    @After
    public void stopApplication(){
        application.stop();
    }
}
