package charlie.xray.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class OreScanner {

	private static final int DEFAULT_RADIUS = 8;

	public List<BlockPos> findDiamondOres(ClientLevel world, BlockPos center) {
		return this.findDiamondOres(world, center, DEFAULT_RADIUS);
	}

	public List<BlockPos> findDiamondOres(ClientLevel world, BlockPos center, int radius) {
		List<BlockPos> diamondOres = new ArrayList<>();

		if (world == null || center == null || radius < 0) {
			return diamondOres;
		}

		BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					currentPos.set(
							center.getX() + x,
							center.getY() + y,
							center.getZ() + z
					);

					Block block = world.getBlockState(currentPos).getBlock();

					if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
						diamondOres.add(currentPos.immutable());
					}
				}
			}
		}

		return diamondOres;
	}
}
