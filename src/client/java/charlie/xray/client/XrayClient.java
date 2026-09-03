package charlie.xray.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class XrayClient implements ClientModInitializer {

	// 儲存 XRay 是否開啟，預設關閉
	private boolean xrayActive = false;

	private final OreScanner oreScanner = new OreScanner();

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
						int diamondOreCount = this.oreScanner.findDiamondOres(
								client.level,
								client.player.blockPosition()
						).size();

						client.player.sendSystemMessage(
								Component.literal("掃描到 " + diamondOreCount + " 顆鑽石礦")
						);
					}
				}
			}
		});
	}
}
