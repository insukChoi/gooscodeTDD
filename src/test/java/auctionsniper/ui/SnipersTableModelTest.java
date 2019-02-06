package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SnipersTableModelTest {

  private TableModelListener listener = mock(TableModelListener.class);
  private final SnipersTableModel model = new SnipersTableModel();

  @BeforeEach
  public void attachModelListener() {
    model.addTableModelListener(listener);
  }

  @Test
  public void hasEnoughColumns() {
    MatcherAssert.assertThat(model.getColumnCount(), CoreMatchers.equalTo(Column.values().length));
  }

  @Test
  public void setsSniperValuesInColumns() {
    model.sniperStateChanged(new SniperSnapshot("item id", 555, 666, SniperState.BIDDING));

    assertColumnEquals(Column.ITEM_IDENTIFIER, "item id");
    assertColumnEquals(Column.LAST_PRICE, 555);
    assertColumnEquals(Column.LAST_BID, 666);
    assertColumnEquals(Column.SNIPER_STATE, SnipersTableModel.textFor(SniperState.BIDDING));

    verify(listener).tableChanged(refEq(new TableModelEvent(model, 0)));
  }

  @Test
  public void setsUpColumnHeadings(){
    for (Column column : Column.values()){
      assertEquals(column.name , model.getColumnName(column.ordinal()));
    }
  }

  private void assertColumnEquals(Column column, Object expected) {
    final int rowIndex = 0;
    final int columnIndex = column.ordinal();
    assertEquals(expected, model.getValueAt(rowIndex, columnIndex));
  }
}