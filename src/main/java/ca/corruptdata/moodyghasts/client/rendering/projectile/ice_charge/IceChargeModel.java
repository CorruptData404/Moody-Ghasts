package ca.corruptdata.moodyghasts.client.rendering.projectile.ice_charge;


import ca.corruptdata.moodyghasts.MoodyGhasts;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class IceChargeModel extends EntityModel<EntityRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ice_charge"), "main");

	private final ModelPart ice_charge;
	private final ModelPart face_spikes;
	private final ModelPart top_spike;
	private final ModelPart west_spike;
	private final ModelPart east_spike;
	private final ModelPart north_spike;
	private final ModelPart south_spike;
	private final ModelPart bottom_spike;
	private final ModelPart corner_spikes;
	private final ModelPart top_spike1;
	private final ModelPart top_spike2;
	private final ModelPart top_spike3;
	private final ModelPart top_spike4;
	private final ModelPart bottom_spike1;
	private final ModelPart bottom_spike2;
	private final ModelPart bottom_spike4;
	private final ModelPart bottom_spike3;
	private final ModelPart snowflake;

	public IceChargeModel(ModelPart root) {
		super(root);
		this.ice_charge = root.getChild("ice_charge");
		this.face_spikes = this.ice_charge.getChild("face_spikes");
		this.top_spike = this.face_spikes.getChild("top_spike");
		this.west_spike = this.face_spikes.getChild("west_spike");
		this.east_spike = this.face_spikes.getChild("east_spike");
		this.north_spike = this.face_spikes.getChild("north_spike");
		this.south_spike = this.face_spikes.getChild("south_spike");
		this.bottom_spike = this.face_spikes.getChild("bottom_spike");
		this.corner_spikes = this.ice_charge.getChild("corner_spikes");
		this.top_spike1 = this.corner_spikes.getChild("top_spike1");
		this.top_spike2 = this.corner_spikes.getChild("top_spike2");
		this.top_spike3 = this.corner_spikes.getChild("top_spike3");
		this.top_spike4 = this.corner_spikes.getChild("top_spike4");
		this.bottom_spike1 = this.corner_spikes.getChild("bottom_spike1");
		this.bottom_spike2 = this.corner_spikes.getChild("bottom_spike2");
		this.bottom_spike4 = this.corner_spikes.getChild("bottom_spike4");
		this.bottom_spike3 = this.corner_spikes.getChild("bottom_spike3");
		this.snowflake = root.getChild("snowflake");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition ice_charge = partdefinition.addOrReplaceChild("ice_charge", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -4.25F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.25F, 0.0F));

		PartDefinition face_spikes = ice_charge.addOrReplaceChild("face_spikes", CubeListBuilder.create(), PartPose.offset(0.0F, 11.75F, -0.5F));

		PartDefinition top_spike = face_spikes.addOrReplaceChild("top_spike", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 0.5F));

		PartDefinition cube_r1 = top_spike.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition west_spike = face_spikes.addOrReplaceChild("west_spike", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -11.5F, 0.5F, 0.0F, 1.5708F, 1.5708F));

		PartDefinition cube_r2 = west_spike.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition east_spike = face_spikes.addOrReplaceChild("east_spike", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -11.5F, 0.5F, 0.0F, -1.5708F, -1.5708F));

		PartDefinition cube_r3 = east_spike.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition north_spike = face_spikes.addOrReplaceChild("north_spike", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.5F, -4.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r4 = north_spike.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition south_spike = face_spikes.addOrReplaceChild("south_spike", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.5F, 5.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r5 = south_spike.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bottom_spike = face_spikes.addOrReplaceChild("bottom_spike", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.0F, 0.5F, 0.0F, 0.0F, -3.1416F));

		PartDefinition cube_r6 = bottom_spike.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -7.0F, -1.5F, 0.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition corner_spikes = ice_charge.addOrReplaceChild("corner_spikes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0107F, 0.0F));

		PartDefinition top_spike1 = corner_spikes.addOrReplaceChild("top_spike1", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.75F, -3.2F, -3.75F, 0.7854F, 0.7854F, 0.0F));

		PartDefinition cube_r7 = top_spike1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition top_spike2 = corner_spikes.addOrReplaceChild("top_spike2", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.75F, -3.2F, 3.75F, -0.7854F, -0.7854F, 0.0F));

		PartDefinition cube_r8 = top_spike2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition top_spike3 = corner_spikes.addOrReplaceChild("top_spike3", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -3.2F, 3.75F, -0.7854F, 0.7854F, 0.0F));

		PartDefinition cube_r9 = top_spike3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition top_spike4 = corner_spikes.addOrReplaceChild("top_spike4", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, -3.2F, -3.75F, 0.7854F, -0.7854F, 0.0F));

		PartDefinition cube_r10 = top_spike4.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition bottom_spike1 = corner_spikes.addOrReplaceChild("bottom_spike1", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.75F, 3.2F, -3.75F, 2.3562F, 0.7854F, 0.0F));

		PartDefinition cube_r11 = bottom_spike1.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition bottom_spike2 = corner_spikes.addOrReplaceChild("bottom_spike2", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.75F, 3.2F, 3.75F, -2.3562F, -0.7854F, 0.0F));

		PartDefinition cube_r12 = bottom_spike2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bottom_spike4 = corner_spikes.addOrReplaceChild("bottom_spike4", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, 3.2F, -3.75F, 2.3562F, -0.7854F, 0.0F));

		PartDefinition cube_r13 = bottom_spike4.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bottom_spike3 = corner_spikes.addOrReplaceChild("bottom_spike3", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.75F, 3.2F, 3.75F, -2.3562F, 0.7854F, 0.0F));

		PartDefinition cube_r14 = bottom_spike3.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -4.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition snowflake = partdefinition.addOrReplaceChild("snowflake", CubeListBuilder.create().texOffs(8, 20).addBox(-3.5F, -15.25F, 0.0F, 7.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 36, 36);
	}

	public ModelPart getDecal() { return snowflake; }
	public ModelPart getBody()  { return ice_charge; }

	@Override
	public void setupAnim(EntityRenderState state) {
		super.setupAnim(state);

		float degreesPerTick = -360.0F / 30.0F; // 360 degrees over 1.5s (30 ticks at 20 ticks/sec)
		float degrees = (state.ageInTicks * degreesPerTick) % 360.0F;
		this.ice_charge.xRot = degrees * Mth.DEG_TO_RAD;

	}
}