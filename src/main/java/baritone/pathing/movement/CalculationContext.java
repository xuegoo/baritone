/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.pathing.movement;

import baritone.Baritone;
import baritone.api.IBaritone;
import baritone.api.pathing.movement.ActionCosts;
import baritone.cache.WorldData;
import baritone.utils.BlockStateInterface;
import baritone.utils.ToolSet;
import baritone.utils.pathing.BetterWorldBorder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Brady
 * @since 8/7/2018
 */
public class CalculationContext {

    private static final ItemStack STACK_BUCKET_WATER = new ItemStack(Items.WATER_BUCKET);

    public final IBaritone baritone;
    public final World world;
    public final WorldData worldData;
    public final BlockStateInterface bsi;
    public final ToolSet toolSet;
    public final boolean hasWaterBucket;
    public final boolean hasThrowaway;
    public final boolean canSprint;
    public final double placeBlockCost;
    public final boolean allowBreak;
    public final boolean allowParkour;
    public final boolean allowParkourPlace;
    public final boolean allowJumpAt256;
    public final boolean assumeWalkOnWater;
    public final boolean allowDiagonalDescend;
    public final int maxFallHeightNoWater;
    public final int maxFallHeightBucket;
    public final double waterWalkSpeed;
    public final double breakBlockAdditionalCost;
    public final double jumpPenalty;
    public final double walkOnWaterOnePenalty;
    public final BetterWorldBorder worldBorder;

    public CalculationContext(IBaritone baritone) {
        this(baritone, false);
    }

    public CalculationContext(IBaritone baritone, boolean forUseOnAnotherThread) {
        this.baritone = baritone;
        EntityPlayerSP player = baritone.getPlayerContext().player();
        this.world = baritone.getPlayerContext().world();
        this.worldData = (WorldData) baritone.getWorldProvider().getCurrentWorld();
        this.bsi = new BlockStateInterface(world, worldData, forUseOnAnotherThread); // TODO TODO TODO
        // new CalculationContext() needs to happen, can't add an argument (i'll beat you), can we get the world provider from currentlyTicking?
        this.toolSet = new ToolSet(player);
        this.hasThrowaway = Baritone.settings().allowPlace.get() && MovementHelper.throwaway(baritone.getPlayerContext(), false);
        this.hasWaterBucket = Baritone.settings().allowWaterBucketFall.get() && InventoryPlayer.isHotbar(player.inventory.getSlotFor(STACK_BUCKET_WATER)) && !world.provider.isNether();
        this.canSprint = Baritone.settings().allowSprint.get() && player.getFoodStats().getFoodLevel() > 6;
        this.placeBlockCost = Baritone.settings().blockPlacementPenalty.get();
        this.allowBreak = Baritone.settings().allowBreak.get();
        this.allowParkour = Baritone.settings().allowParkour.get();
        this.allowParkourPlace = Baritone.settings().allowParkourPlace.get();
        this.allowJumpAt256 = Baritone.settings().allowJumpAt256.get();
        this.assumeWalkOnWater = Baritone.settings().assumeWalkOnWater.get();
        this.allowDiagonalDescend = Baritone.settings().allowDiagonalDescend.get();
        this.maxFallHeightNoWater = Baritone.settings().maxFallHeightNoWater.get();
        this.maxFallHeightBucket = Baritone.settings().maxFallHeightBucket.get();
        int depth = EnchantmentHelper.getDepthStriderModifier(player);
        if (depth > 3) {
            depth = 3;
        }
        float mult = depth / 3.0F;
        this.waterWalkSpeed = ActionCosts.WALK_ONE_IN_WATER_COST * (1 - mult) + ActionCosts.WALK_ONE_BLOCK_COST * mult;
        this.breakBlockAdditionalCost = Baritone.settings().blockBreakAdditionalPenalty.get();
        this.jumpPenalty = Baritone.settings().jumpPenalty.get();
        this.walkOnWaterOnePenalty = Baritone.settings().walkOnWaterOnePenalty.get();
        // why cache these things here, why not let the movements just get directly from settings?
        // because if some movements are calculated one way and others are calculated another way,
        // then you get a wildly inconsistent path that isn't optimal for either scenario.
        this.worldBorder = new BetterWorldBorder(world.getWorldBorder());
    }

    public final IBaritone getBaritone() {
        return baritone;
    }

    public IBlockState get(int x, int y, int z) {
        return bsi.get0(x, y, z); // laughs maniacally
    }

    public boolean isLoaded(int x, int z) {
        return bsi.isLoaded(x, z);
    }

    public IBlockState get(BlockPos pos) {
        return get(pos.getX(), pos.getY(), pos.getZ());
    }

    public Block getBlock(int x, int y, int z) {
        return get(x, y, z).getBlock();
    }

    public boolean canPlaceThrowawayAt(int x, int y, int z) {
        if (!hasThrowaway) { // only true if allowPlace is true, see constructor
            return false;
        }
        if (isPossiblyProtected(x, y, z)) {
            return false;
        }
        return worldBorder.canPlaceAt(x, z); // TODO perhaps MovementHelper.canPlaceAgainst could also use this?
    }

    public boolean canBreakAt(int x, int y, int z) {
        if (!allowBreak) {
            return false;
        }
        return !isPossiblyProtected(x, y, z);
    }

    public boolean isPossiblyProtected(int x, int y, int z) {
        // TODO more protection logic here; see #220
        return false;
    }
}
