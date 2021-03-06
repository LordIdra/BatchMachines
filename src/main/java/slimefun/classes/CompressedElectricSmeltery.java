package slimefun.classes;


import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.cscorelib2.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricSmeltery;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;

import me.mrCookieSlime.Slimefun.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;






public class CompressedElectricSmeltery extends ElectricSmeltery {

    private int compression;
    private Config cfg = SlimefunPlugin.getCfg();
    private int tickSpeed = 20 / cfg.getInt("URID.custom-ticker-delay");
    
    public CompressedElectricSmeltery(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int compressionLevel) {
		super(category, item, recipeType, recipe);
		
		compression = compressionLevel;
	}
    
    @Override
    protected MachineRecipe findNextRecipe(BlockMenu inv) {
        Map<Integer, ItemStack> inventory = new HashMap<>();

        for (int slot : getInputSlots()) {
            ItemStack item = inv.getItemInSlot(slot);

            if (item != null) {
                inventory.put(slot, item);
            }
        }

        Map<Integer, Integer> found = new HashMap<>();

        for (MachineRecipe recipe : recipes) {
        	
        	ItemStack[] outputs = recipe.getOutput().clone();
        	
        	for (int i = 0; i < outputs.length; i++) {
        		ItemStack currentOutput = new ItemStack(outputs[i]);
        		currentOutput.setAmount(currentOutput.getAmount() * compression);
        		outputs[i] = currentOutput;
        	}
        	
        	
        	recipe = new MachineRecipe(recipe.getTicks() / tickSpeed, recipe.getInput(), outputs);
        	
            for (ItemStack input : recipe.getInput()) {
                for (int slot : getInputSlots()) {
                    if (SlimefunUtils.isItemSimilar(inventory.get(slot), input, true)) {
                    	if (inventory.get(slot).getAmount() >= input.getAmount() * compression) {
                    		found.put(slot, input.getAmount());
                    		break;
                    	}
                    }
                }
            }

            if (found.size() == recipe.getInput().length) {
                if (!InvUtils.fitAll(inv.toInventory(), recipe.getOutput(), getOutputSlots())) {
                    return null;
                }

                for (Map.Entry<Integer, Integer> entry : found.entrySet()) {
                    inv.consumeItem(entry.getKey(), entry.getValue() * compression);
                }

                return recipe;
            } else {
                found.clear();
            }
        }

        return null;
    }
}