/**
 * 
 */
package auctionsniper.ui;

import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;

public class SwingThreadSniperListener implements SniperListener {
  private final SnipersTableModel snipers;

  public SwingThreadSniperListener(SnipersTableModel snipers) {
    this.snipers = snipers;
  }

  @Override
  public void sniperStateChanged(SniperSnapshot snapshot) {
    snipers.sniperStateChanged(snapshot);
  }
}