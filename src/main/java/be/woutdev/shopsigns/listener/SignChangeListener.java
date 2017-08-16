package be.woutdev.shopsigns.listener;

import be.woutdev.shopsigns.model.ShopSign.ShopSignType;
import java.math.BigDecimal;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Created by Wout on 13/08/2017.
 */
public class SignChangeListener implements Listener
{
    @EventHandler
    public void onSignChange(SignChangeEvent e)
    {
        Player p = e.getPlayer();
        String[] lines = e.getLines();

        if (lines[0].equalsIgnoreCase("[Buy]") ||
            lines[0].equalsIgnoreCase("[Sell]"))
        {
            if (!p.isOp())
            {
                p.sendMessage(ChatColor.RED + "Economy: You are not allowed to place shop signs!");
                e.getBlock().breakNaturally();
                return;
            }

            if (lines.length != 4 || lines[1].isEmpty() || lines[2].isEmpty() || lines[3].isEmpty())
            {
                p.sendMessage(ChatColor.RED + "Economy: Invalid sign format!");
                e.getBlock().breakNaturally();
                return;
            }

            ShopSignType type = lines[0].equalsIgnoreCase("[Buy]") ? ShopSignType.BUY : ShopSignType.SELL;

            Material material = Material.matchMaterial(lines[1]);

            if (material == null)
            {
                p.sendMessage(ChatColor.RED + "Economy: Invalid material!");
                e.getBlock().breakNaturally();
                return;
            }

            int amount;
            try
            {
                amount = Integer.parseInt(lines[2]);

                if (amount > 64 || amount < 1)
                    throw new NumberFormatException("invalid amount");
            }
            catch (NumberFormatException ex)
            {
                p.sendMessage(ChatColor.RED + "Economy: Invalid amount!");
                e.getBlock().breakNaturally();
                return;
            }

            BigDecimal price;
            try
            {
                price = new BigDecimal(lines[3]);

                if (price.doubleValue() < 0)
                    throw new NumberFormatException("invalid price");
            }
            catch (NumberFormatException ex)
            {
                p.sendMessage(ChatColor.RED + "Economy: Invalid price!");
                e.getBlock().breakNaturally();
                return;
            }

            if (type == ShopSignType.BUY)
                e.setLine(0, ChatColor.GREEN + "[Buy]");
            else
                e.setLine(0, ChatColor.DARK_RED + "[Sell]");

            p.sendMessage(ChatColor.GREEN + "Successfully created shop sign!");
        }
    }
}
