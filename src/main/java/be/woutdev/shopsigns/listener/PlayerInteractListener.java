package be.woutdev.shopsigns.listener;

import be.woutdev.economy.api.EconomyAPI;
import be.woutdev.economy.api.account.Account;
import be.woutdev.economy.api.transaction.Transaction;
import be.woutdev.economy.api.transaction.TransactionType;
import be.woutdev.shopsigns.model.ShopSign;
import be.woutdev.shopsigns.model.ShopSign.ShopSignType;
import java.math.BigDecimal;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Wout on 13/08/2017.
 */
public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK ||
            e.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            Block block = e.getClickedBlock();

            ShopSign sign = ShopSign.fromBlock(block);

            if (sign == null)
                return;

            Account account = EconomyAPI.getAPI().getAccount(e.getPlayer());

            if (sign.getType() == ShopSignType.BUY) {
                if (account.getBalance().doubleValue() < sign.getPrice().doubleValue()) {
                    e.getPlayer().sendMessage(ChatColor.RED + "Economy: You have insufficient funds!");
                    return;
                }

                if (e.getPlayer().getInventory().firstEmpty() == -1) {
                    e.getPlayer().sendMessage(ChatColor.RED + "Economy: You do not have an empty slot available!");
                    return;
                }

                Transaction transaction = EconomyAPI.getAPI().createTransaction(account, TransactionType.WITHDRAW, sign.getPrice());

                EconomyAPI.getAPI().transact(transaction).addListener((t) -> {
                    switch(t.getResult().getStatus())
                    {
                        case SUCCESS:
                            e.getPlayer().getInventory().addItem(new ItemStack(sign.getMaterial(), sign.getAmount()));
                            e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully brought " + sign.getAmount() + "x " + sign.getMaterial().name() + " for " + EconomyAPI.getAPI().format(sign.getPrice()));
                            e.getPlayer().updateInventory();
                            break;
                        default:
                            e.getPlayer().sendMessage(ChatColor.RED + "Economy: Error in transaction. (" + t.getResult().getStatus().toString() + ")");
                            break;
                    }
                });
            }
            else
            {
                ItemStack toSell = new ItemStack(sign.getMaterial(), sign.getAmount());

                int amountPlayerHas = 0;

                for (ItemStack i : e.getPlayer().getInventory())
                {
                    if (i == null)
                        continue;

                    if (i.isSimilar(toSell))
                        amountPlayerHas += i.getAmount();
                }

                if (amountPlayerHas < toSell.getAmount())
                {
                    e.getPlayer().sendMessage(ChatColor.RED + "Economy: You have insufficient items!");
                    return;
                }

                int toRemove = 0;

                if (e.getPlayer().isSneaking())
                {
                    toRemove = amountPlayerHas;
                }
                else
                {
                    toRemove = sign.getAmount();
                }

                Iterator<ItemStack> it = e.getPlayer().getInventory().iterator();

                while(it.hasNext())
                {
                    ItemStack i = it.next();

                    if (i == null)
                        continue;

                    if (i.isSimilar(toSell))
                    {
                        if (i.getAmount() == toRemove)
                        {
                            e.getPlayer().getInventory().remove(i);
                            toRemove = 0;
                            break;
                        }
                        else if (i.getAmount() > toRemove)
                        {
                            i.setAmount(i.getAmount() - toRemove);
                            toRemove = 0;
                            break;
                        }
                        else
                        {
                            e.getPlayer().getInventory().remove(i);
                            toRemove -= i.getAmount();
                        }
                    }
                }

                if (toRemove != 0)
                {
                    return;
                }

                e.getPlayer().updateInventory();

                Transaction transaction = EconomyAPI.getAPI().createTransaction(account,
                    TransactionType.DEPOSIT,
                    e.getPlayer().isSneaking()
                        ? ((sign.getPrice().divide(new BigDecimal(sign.getAmount())).multiply(new BigDecimal(amountPlayerHas))))
                        : sign.getPrice());

                int finalAmountPlayerHas = amountPlayerHas;

                EconomyAPI.getAPI().transact(transaction).addListener((t) -> {
                    switch(t.getResult().getStatus())
                    {
                        case SUCCESS:
                            e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully sold " + (e.getPlayer().isSneaking() ? finalAmountPlayerHas
                                : sign.getAmount()) + "x " + sign.getMaterial().name() + " for " + EconomyAPI.getAPI().format(e.getPlayer().isSneaking() ? sign.getPrice().divide(new BigDecimal(sign.getAmount())).multiply(new BigDecimal(finalAmountPlayerHas)) : sign.getPrice()));
                            break;
                        default:
                            e.getPlayer().sendMessage(ChatColor.RED + "Economy: Error in transaction. (" + t.getResult().getStatus().toString() + ")");
                            break;
                    }
                });
            }
        }
    }
}
