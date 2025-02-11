package drunkmafia.thaumicinfusion.common.intergration;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.item.TIItems;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemEssence;

import java.util.ArrayList;

/**
 * Created by DrunkMafia on 08/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class ThaumcraftIntergration {

    public static void init() {
        ShapedArcaneRecipe essentiaRecipe = null;
        ItemStack essentiaBlock = null;

        for (Aspect aspect : Aspect.aspects.values()) {

            for (int i = 0; i <= 2; i++) {
                ItemStack stack = getEssentiaBlock(aspect, i);

                ItemStack item;
                if (i == 0) {
                    item = new ItemStack(ConfigItems.itemEssence, 1, 1);
                    ((ItemEssence) item.getItem()).setAspects(item, new AspectList().add(aspect, 8));
                } else if (i == 1) {
                    item = getEssentiaBlock(aspect, 0);
                } else if (i == 2) {
                    item = getEssentiaBlock(aspect, 1);
                } else continue;

                ShapedArcaneRecipe recipe = ThaumcraftApi.addArcaneCraftingRecipe("ESSENTIABLOCKS", stack, new AspectList().add(Aspect.ENTROPY, 4), "PP", "PP", Character.valueOf('P'), item);
                if (essentiaRecipe == null)
                    essentiaRecipe = recipe;
                if (essentiaBlock == null)
                    essentiaBlock = stack;
            }
        }

        ResearchCategories.registerCategory("THAUMICINFUSION", new ResourceLocation(ModInfo.MODID, "textures/research/r_ti.png"), new ResourceLocation(ModInfo.MODID, "textures/gui/r_tibg.png"));

        InfusionRecipe infusionRecipe = ThaumcraftApi.addInfusionCraftingRecipe("FOCUSINFUSION", new ItemStack(TIItems.focusInfusing), 4, (new AspectList()).add(Aspect.EARTH, 25).add(Aspect.ARMOR, 25).add(Aspect.ORDER, 25).add(Aspect.MIND, 10), new ItemStack(ConfigItems.itemFocusWarding), new ItemStack[]{new ItemStack(ConfigItems.itemResource, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(Items.quartz), new ItemStack(ConfigItems.itemShard, 1, 4), new ItemStack(ConfigItems.itemResource, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(Items.quartz), new ItemStack(ConfigItems.itemShard, 1, 4)});

        new ResearchItem("FOCUSINFUSION", "THAUMICINFUSION", (new AspectList()).add(Aspect.EARTH, 6).add(Aspect.ARMOR, 3).add(Aspect.ORDER, 3).add(Aspect.MIND, 3), -2, 0, 2, new ItemStack(TIItems.focusInfusing)).setPages(new ResearchPage("tc.research_page.FOCUSINFUSION.1"), new ResearchPage("tc.research_page.FOCUSINFUSION.2"), new ResearchPage(infusionRecipe)).registerResearchItem();
        new ResearchItem("ESSENTIABLOCKS", "THAUMICINFUSION", new AspectList().add(Aspect.ORDER, 3).add(Aspect.MAGIC, 3), 2, 0, 2, essentiaBlock).setPages(new ResearchPage("tc.research_page.ESSENTIABLOCKS.1"), new ResearchPage(essentiaRecipe)).registerResearchItem();

        new ResearchItem("ASPECTEFFECTS", "THAUMICINFUSION", new AspectList(), 0, 2, 2, new ResourceLocation("thaumcraft", "textures/misc/r_aspects.png")).setPages(getPages()).setAutoUnlock().registerResearchItem();
    }

    static ItemStack getEssentiaBlock(Aspect aspect, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("aspectTag", aspect.getTag());
        ItemStack stack = new ItemStack(TIBlocks.essentiaBlock);
        stack.setItemDamage(meta);
        stack.setTagCompound(tag);
        stack.setStackDisplayName(aspect.getName() + (meta != 0 ? (meta == 1 ? ThaumicInfusion.translate("key.essentiaBlock.brick") : ThaumicInfusion.translate("key.essentiaBlock.chiseled")) : ""));
        return stack;
    }

    private static ResearchPage[] getPages() {
        Aspect[] aspects = AspectHandler.getAspects();
        AspectList current = new AspectList();
        ArrayList<ResearchPage> pages = new ArrayList<ResearchPage>();

        int index = 0;
        for (Aspect aspect : aspects) {
            if (aspect != null) {
                current.add(aspect, AspectHandler.getCostOfEffect(aspect));
                if (index == 1) {
                    pages.add(new AspectEffectPage(current));
                    current = new AspectList();
                    index = 0;
                } else
                    index++;
            }
        }
        ResearchPage[] researchPages = new ResearchPage[pages.size()];
        for (int p = 0; p < researchPages.length; p++)
            researchPages[p] = pages.get(p);

        return researchPages;
    }


    public static class AspectEffectPage extends ResearchPage {

        public AspectList aspects;

        public AspectEffectPage(AspectList aspects) {
            super("");
            this.aspects = aspects;
        }

        @Override
        public String getTranslatedText() {
            String str = "";
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect != null) {
                    ResourceLocation location = aspect.getImage();
                    str += "<IMG>" + location.getResourceDomain() + ":" + location.getResourcePath() + ":0:0:255:255:0.125</IMG>" + aspect.getName() + " " + ThaumicInfusion.translate("ti.cost") + ": " + AspectHandler.getCostOfEffect(aspect) + " " + ThaumicInfusion.translate("ti.effect_info." + aspect.getName().toUpperCase()) + "\n";
                }
            }
            return str;
        }
    }
}