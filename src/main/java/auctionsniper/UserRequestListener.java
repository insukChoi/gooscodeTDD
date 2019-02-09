package auctionsniper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.EventListener;

public interface UserRequestListener extends EventListener {
  void joinAuction(Item item);
  
  public static class Item {
    public final String identifier;

    public Item(String identifier) {
      this.identifier = identifier;
    }


    @Override
    public boolean equals(Object obj) { return EqualsBuilder.reflectionEquals(this, obj); }
    @Override
    public int hashCode() { return HashCodeBuilder.reflectionHashCode(this); }
    @Override
    public String toString() { return "Item: " + identifier ;}

  }
}
