package charlie.xray.client;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;

public class OreRenderer {

	// 亮青色外框，方便在洞穴或地下看出鑽石礦位置。
	private static final int DIAMOND_COLOR = ARGB.color(220, 0, 255, 255);
	private static final float BOX_PADDING = 0.01F;
	private static final float LINE_WIDTH = 1;
	private static final GizmoStyle DIAMOND_BOX_STYLE = GizmoStyle.stroke(DIAMOND_COLOR, LINE_WIDTH);

	private final List<BlockPos> diamondOres = new ArrayList<>();

	public void register() {
		LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
			if (this.diamondOres.isEmpty()) {
				return;
			}

			// 參考 AdvancedXRay 的 outline 思路：把每個礦物方塊畫成外框。
			// 這裡使用 Minecraft 26.2 內建 Gizmos，setAlwaysOnTop 讓外框可隔著方塊看見。
			try (Gizmos.TemporaryCollection ignored = context.levelRenderer().collectPerFrameRenderThreadGizmos()) {
				for (BlockPos pos : List.copyOf(this.diamondOres)) {
					Gizmos.cuboid(pos, BOX_PADDING, DIAMOND_BOX_STYLE).setAlwaysOnTop();
				}
			}
		});
	}

	public void setDiamondOres(List<BlockPos> diamondOres) {
		this.diamondOres.clear();

		for (BlockPos pos : diamondOres) {
			this.diamondOres.add(pos.immutable());
		}
	}

	public void clear() {
		this.diamondOres.clear();
	}
}
