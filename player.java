// this file is for storing each players information

public class player
{

        private final String name;
        private int money;
        private int location;

        public player(String name, int startingMoney)
        {
            this.name = name;
            this.money = startingMoney;
            this.location = 0;
        }

        public String getName()
        {
            return name;
        }

        public int getMoney()
        {
            return money;
        }

        public int getLocation()
        {
            return location;
        }

        public void setLocation(int location)
        {
            this.location = location;
        }

        public void setMoney(int money)
        {
            this.money += money;
        }

        public void subtractMoney(int amount)
        {
            this.money -= amount;
        }

    public void addMoney(int i)
    {
            this.money += i;
    }
}
