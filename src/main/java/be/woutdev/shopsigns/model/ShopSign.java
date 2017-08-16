package be.woutdev.shopsigns.model;

import java.math.BigDecimal;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * Created by Wout on 13/08/2017.
 */
public class ShopSign
{
    private final ShopSignType type;
    private final Material material;
    private final int amount;
    private final BigDecimal price;

    public ShopSign(ShopSignType type, Material material, int amount, BigDecimal price) {
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.price = price;
    }

    public ShopSignType getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public static ShopSign fromBlock(Block block)
    {
        if (!(block.getState() instanceof Sign))
            return null;

        Sign sign = (Sign) block.getState();

        String[] lines = sign.getLines();

        if (!lines[0].equalsIgnoreCase(ChatColor.GREEN + "[Buy]") &&
            !lines[0].equalsIgnoreCase(ChatColor.DARK_RED + "[Sell]"))
            return null;

        ShopSignType type = lines[0].equalsIgnoreCase(ChatColor.GREEN + "[Buy]") ? ShopSignType.BUY : ShopSignType.SELL;

        Material material = Material.matchMaterial(lines[1]);

        if (material == null)
            return null;

        int amount;
        try
        {
            amount = Integer.parseInt(lines[2]);

            if (amount > 64 || amount < 1)
                throw new NumberFormatException("amount invalid");
        }
        catch (NumberFormatException ex)
        {
            return null;
        }

        BigDecimal price;
        try
        {
            price = new BigDecimal(lines[3]);

            if (price.doubleValue() < 0)
                throw new NumberFormatException("price invalid");
        }
        catch (NumberFormatException ex)
        {
            return null;
        }

        return new ShopSign(type, material, amount, price);
    }

    public enum ShopSignType
    {
        BUY,
        SELL,
    }
}
