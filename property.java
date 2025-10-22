// this file is for storing the property information

public class property
{
    private final String name;
    private final int purchasePrice;
    private final int baseRent;
    private String owner;
    private int houses; // 0-4
    private boolean hotel;

    public property(String name, int purchasePrice, int rentPrice)
    {
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.baseRent = rentPrice;
        this.owner = "unowned";
        this.houses = 0;
        this.hotel = false;
    }

    public String getName() { return name; }
    public int getPurchasePrice() { return purchasePrice; }
    public int getBaseRent() { return baseRent; }
    public String getOwner() { return owner; }
    public void setOwner(String o) { this.owner = o; }

    public int getHouses() { return houses; }
    public void setHouses(int h) { this.houses = h; }
    public boolean hasHotel() { return hotel; }
    public void setHotel(boolean h) { this.hotel = h; }

    public int getRentPrice() {
        if(hotel) return baseRent * 5;
        if(houses>0) return baseRent + houses*baseRent;
        return baseRent;
    }
}