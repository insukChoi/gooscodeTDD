package auctionsniper.endtoend;

import auctionsniper.Main;
import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JTableDriver;
import com.objogate.wl.swing.driver.JTableHeaderDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;
import com.objogate.wl.swing.matcher.IterableComponentsMatcher;
import com.objogate.wl.swing.matcher.JLabelTextMatcher;

import javax.swing.table.JTableHeader;

import static java.lang.String.valueOf;

public class AuctionSniperDriver extends JFrameDriver {
    public AuctionSniperDriver(int timeoutMillis){
        super(new GesturePerformer(),
                JFrameDriver.topLevelFrame(
                        named(Main.MAIN_WINDOW_NAME),
                        showingOnScreen()
                ),
                new AWTEventQueueProber(timeoutMillis, 100));
    }

    public void showsSniperStatus(String itemId, int lastPrice, int lastBid, String statusText){
        JTableDriver table = new JTableDriver(this);
        table.hasRow(IterableComponentsMatcher.matching(
                JLabelTextMatcher.withLabelText(itemId),
                JLabelTextMatcher.withLabelText(valueOf(lastPrice)),
                JLabelTextMatcher.withLabelText(valueOf(lastBid)),
                JLabelTextMatcher.withLabelText(statusText)
        ));
    }

    public void hasColumnTitle() {
        JTableHeaderDriver headers = new JTableHeaderDriver(this, JTableHeader.class);
        headers.hasHeaders(IterableComponentsMatcher.matching(
                JLabelTextMatcher.withLabelText("Item"),
                JLabelTextMatcher.withLabelText("Last Price"),
                JLabelTextMatcher.withLabelText("Last Bid"),
                JLabelTextMatcher.withLabelText("State")
        ));
    }
}
