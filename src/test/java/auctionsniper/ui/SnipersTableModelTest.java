package auctionsniper.ui;

import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import javax.swing.event.TableModelListener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

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
      //model.addSniper(joining);

      assertColumnEquals(Column.ITEM_IDENTIFIER, "item id");
      assertColumnEquals(Column.LAST_PRICE, 555);
      assertColumnEquals(Column.LAST_BID, 666);
      assertColumnEquals(Column.SNIPER_STATE, SnipersTableModel.textFor(SniperState.BIDDING));
  }

  @Test
  public void setsUpColumnHeadings(){
      for (Column column : Column.values()){
        assertEquals(column.name , model.getColumnName(column.ordinal()));
      }
  }

  @Test
  public void notifiesListenersWhenAddingASniper(){
      SniperSnapshot joining = SniperSnapshot.joining("item123");
      assertEquals(0, model.getRowCount());

      model.addSniper(joining);

      assertEquals(1, model.getRowCount());
      assertRowMatchesSnapshot(0, joining);
  }

  @Test
  public void holdsSnipersInAdditionOrder(){
      model.addSniper(SniperSnapshot.joining("item 0"));
      model.addSniper(SniperSnapshot.joining("item 1"));

      assertEquals("item 0", cellValue(0, Column.ITEM_IDENTIFIER));
      assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER));
  }

  private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
      assertEquals(snapshot.itemId, cellValue(row, Column.ITEM_IDENTIFIER));
      assertEquals(snapshot.lastPrice, cellValue(row, Column.LAST_PRICE));
      assertEquals(snapshot.lastBid, cellValue(row, Column.LAST_BID));
      assertEquals(SnipersTableModel.textFor(snapshot.state), cellValue(row, Column.SNIPER_STATE));
  }

  private Object cellValue(int rowIndex, Column column) {
      return model.getValueAt(rowIndex, column.ordinal());
  }

  private void assertColumnEquals(Column column, Object expected) {
      final int rowIndex = 0;
      final int columnIndex = column.ordinal();
      assertEquals(expected, model.getValueAt(rowIndex, columnIndex));
  }
}