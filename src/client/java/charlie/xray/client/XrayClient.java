package charlie.xray.client;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class XrayClient implements ClientModInitializer {

	// 儲存 XRay 是否開啟，預設關閉
	private boolean xrayActive = false;

	private final OreScanner oreScanner = new OreScanner();

	private final OreRenderer oreRenderer = new OreRenderer();

	// 記住上一次掃描時玩家所在的 chunk，用來判斷是否需要重新掃描與顯示。
	private boolean hasLastScannedChunk = false;
	private int lastScannedChunkX = 0;
	private int lastScannedChunkZ = 0;

	// 在 Minecraft 控制設定中建立一個 XRay 分類
	private final KeyMapping.Category xrayCategory =
			KeyMapping.Category.register(
					Identifier.fromNamespaceAndPath(
							"xray",
							"controls"
					)
			);

	// 註冊 Toggle XRay 按鍵，預設是 X
	private final KeyMapping toggleXrayKey =
			KeyMappingHelper.registerKeyMapping(
					new KeyMapping(
							"key.xray.toggle",
							InputConstants.Type.KEYSYM,
							InputConstants.KEY_X,
							this.xrayCategory
					)
			);

	@Override
	public void onInitializeClient() {
		this.oreRenderer.register();

		// 每個遊戲 tick 檢查玩家是否按下按鍵
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			while (this.toggleXrayKey.consumeClick()) {

				// false 變 true，true 變 false
				this.xrayActive = !this.xrayActive;

				if (client.player != null) {
					String status = this.xrayActive ? "ON" : "OFF";

					client.player.sendSystemMessage(
							Component.literal("XRay: " + status)
					);

					if (this.xrayActive && client.level != null) {
						this.scanAndDisplayDiamondOres(client);
					} else {
						this.resetLastScannedChunk();
						this.oreRenderer.clear();
					}
				}
			}

			if (this.xrayActive && client.player != null && client.level != null) {
				BlockPos currentPlayerPos = client.player.blockPosition();
				int currentChunkX = this.toChunkCoordinate(currentPlayerPos.getX());
				int currentChunkZ = this.toChunkCoordinate(currentPlayerPos.getZ());

				// 玩家還在同一個 chunk 時不更新；進入新 chunk 才重新掃描並重新顯示結果。
				if (!this.isLastScannedChunk(currentChunkX, currentChunkZ)) {
					this.scanAndDisplayDiamondOres(client);
				}
			}
		});
	}

	private void scanAndDisplayDiamondOres(Minecraft client) {
		if (client.player == null || client.level == null) {
			return;
		}

		BlockPos currentPlayerPos = client.player.blockPosition();
		int currentChunkX = this.toChunkCoordinate(currentPlayerPos.getX());
		int currentChunkZ = this.toChunkCoordinate(currentPlayerPos.getZ());

		List<BlockPos> diamondOres = this.oreScanner.findDiamondOresInChunks(
				client.level,
				currentChunkX,
				currentChunkZ
		);

		this.lastScannedChunkX = currentChunkX;
		this.lastScannedChunkZ = currentChunkZ;
		this.hasLastScannedChunk = true;
		this.oreRenderer.setDiamondOres(diamondOres);

		client.player.sendSystemMessage(
				Component.literal("掃描到 " + diamondOres.size() + " 顆鑽石礦")
		);
	}

	private boolean isLastScannedChunk(int chunkX, int chunkZ) {
		return this.hasLastScannedChunk
				&& this.lastScannedChunkX == chunkX
				&& this.lastScannedChunkZ == chunkZ;
	}

	private void resetLastScannedChunk() {
		this.hasLastScannedChunk = false;
		this.lastScannedChunkX = 0;
		this.lastScannedChunkZ = 0;
	}

	private int toChunkCoordinate(int blockCoordinate) {
		return blockCoordinate >> 4;
	}
}
