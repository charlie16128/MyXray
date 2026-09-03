package charlie.xray.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class OreScanner {

	// 沒有指定 chunk 範圍時，預設掃描中心 chunk 加上周圍 1 格，也就是 3x3 chunks。
	private static final int DEFAULT_CHUNK_RADIUS = 1;

	// 使用玩家所在位置推算中心 chunk，並掃描預設 3x3 chunks。
	public List<BlockPos> findDiamondOres(ClientLevel world, BlockPos center) {
		if (center == null) {
			return new ArrayList<>();
		}

		return this.findDiamondOresInChunks(
				world,
				this.toChunkCoordinate(center.getX()),
				this.toChunkCoordinate(center.getZ()),
				DEFAULT_CHUNK_RADIUS
		);
	}

	// 掃描指定中心 chunk 周圍的 chunk 範圍，並回傳鑽石礦座標清單。
	public List<BlockPos> findDiamondOresInChunks(ClientLevel world, int centerChunkX, int centerChunkZ) {
		return this.findDiamondOresInChunks(world, centerChunkX, centerChunkZ, DEFAULT_CHUNK_RADIUS);
	}

	// 掃描指定中心 chunk 周圍 chunkRadius 格、整個世界 Y 軸高度的方塊。
	public List<BlockPos> findDiamondOresInChunks(ClientLevel world, int centerChunkX, int centerChunkZ, int chunkRadius) {
		List<BlockPos> diamondOres = new ArrayList<>();

		// 世界不存在，或 chunk 範圍不合理時，直接回傳空清單避免遊戲崩潰。
		if (world == null || chunkRadius < 0) {
			return diamondOres;
		}

		// MutableBlockPos 可以重複改座標，避免掃描時建立太多暫時物件。
		BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();

		// Y 軸掃世界可建造高度的完整範圍。
		int minY = world.getMinY();
		int maxY = world.getMaxY();

		// 從中心 chunk 往 X/Z 周圍展開，預設 -1 到 +1 就是 3x3 chunks。
		for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
			for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
				int minX = chunkX << 4;
				int maxX = minX + 15;
				int minZ = chunkZ << 4;
				int maxZ = minZ + 15;

				for (int x = minX; x <= maxX; x++) {
					for (int y = minY; y <= maxY; y++) {
						for (int z = minZ; z <= maxZ; z++) {
							currentPos.set(x, y, z);

							Block block = world.getBlockState(currentPos).getBlock();

							// 普通鑽石礦和深板岩鑽石礦都算進掃描結果。
							if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
								diamondOres.add(currentPos.immutable());
							}
						}
					}
				}
			}
		}

		return diamondOres;
	}

	private int toChunkCoordinate(int blockCoordinate) {
		return blockCoordinate >> 4;
	}
}
