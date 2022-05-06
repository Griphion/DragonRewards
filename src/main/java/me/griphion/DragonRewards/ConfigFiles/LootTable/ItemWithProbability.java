package me.griphion.DragonRewards.ConfigFiles.LootTable;

import me.griphion.DragonRewards.Utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemWithProbability {
    public final ItemStack item;
    public double probabilidad; //Probabilidad/chance de que te de el item 0-100(%)
    public int minimo;
    public int maximo;

    public ItemWithProbability(final ItemStack item, double probabilidad, int minimo, int maximo) {
        this.item = item;
        this.probabilidad = probabilidad;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    public List<ItemStack> getRandomAmountOfItem() {
        List<ItemStack> items = new ArrayList<>();
        if (probabilidad <= 0) return items;
        int j = 0;
        if(minimo > maximo){
            for(int i = 0; i < maximo; i++){
                j = stackAndAddItem(items, j);
            }
        }else{
            for (int i = 0; i < maximo; i++) {
                if (Utils.getChance(probabilidad) || i < minimo) {
                    j = stackAndAddItem(items, j);
                }
            }
        }

        return items;
    }

    private int stackAndAddItem(List<ItemStack> items, int j) {
        if (items.size() > j)
            if (Utils.canBeStacked(items.get(j))) {
                items.addAll(Utils.stackItems(items.remove(j).clone(), item.clone()));
                if (Utils.canBeStacked(items.get(j))) return j;
                j++;
                return j;
            }
        items.add(item);
        return j;
    }
}
