package fr.dynamx.common.contentpack.type.objects;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.math.Vector3f;
import fr.dynamx.api.contentpack.object.IShapeContainer;
import fr.dynamx.api.contentpack.object.IShapeProvider;
import fr.dynamx.api.contentpack.object.part.BasePart;
import fr.dynamx.api.contentpack.registry.DefinitionType;
import fr.dynamx.api.contentpack.registry.IPackFilePropertyFixer;
import fr.dynamx.api.contentpack.registry.PackFileProperty;
import fr.dynamx.api.contentpack.registry.SubInfoTypeRegistries;
import fr.dynamx.api.obj.ObjModelPath;
import fr.dynamx.client.renders.model.renderer.ObjObjectRenderer;
import fr.dynamx.client.renders.model.texture.TextureVariantData;
import fr.dynamx.common.DynamXContext;
import fr.dynamx.common.contentpack.DynamXObjectLoaders;
import fr.dynamx.common.contentpack.PackInfo;
import fr.dynamx.common.contentpack.parts.PartShape;
import fr.dynamx.utils.DynamXUtils;
import fr.dynamx.utils.optimization.MutableBoundingBox;
import fr.dynamx.utils.physics.ShapeUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractProp<T extends AbstractProp<?>> extends AbstractItemObject<T> implements IShapeContainer, IShapeProvider {
    @IPackFilePropertyFixer.PackFilePropertyFixer(registries = {SubInfoTypeRegistries.WHEELED_VEHICLES, SubInfoTypeRegistries.PROPS})
    public static final IPackFilePropertyFixer PROPERTY_FIXER = (object, key, value) -> {
        if ("UseHullShape".equals(key))
            return new IPackFilePropertyFixer.FixResult("UseComplexCollisions", true);
        return null;
    };

    @PackFileProperty(configNames = "Translate", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F, required = false, defaultValue = "0 0 0")
    protected Vector3f translation = new Vector3f(0, 0, 0);
    @PackFileProperty(configNames = "Scale", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F, required = false, defaultValue = "1 1 1")
    protected Vector3f scaleModifier = new Vector3f(1, 1, 1);
    @PackFileProperty(configNames = "RenderDistanceSquared", required = false, defaultValue = "4096")
    protected float renderDistance = 4096;
    @PackFileProperty(configNames = "UseComplexCollisions", required = false, defaultValue = "false", description = "common.UseComplexCollisions")
    protected boolean useHullShape = false;
    /*@PackFileProperty(configNames = "CollisionType", required = false, defaultValue = "Simple", type = DefinitionType.DynamXDefinitionTypes.COLLISION_TYPE)
    protected EnumCollisionType collisionType = EnumCollisionType.SIMPLE;*/
    @PackFileProperty(configNames = "Textures", required = false, type = DefinitionType.DynamXDefinitionTypes.STRING_ARRAY_2D)
    protected String[][] texturesArray;

    private final List<BasePart<?>> parts = new ArrayList<>();
    private final List<MutableBoundingBox> collisionBoxes = new ArrayList<>();
    private final List<PartShape<?>> partShapes = new ArrayList<>();
    private final Map<Byte, TextureVariantData> textures = new HashMap<>();

    protected CompoundCollisionShape compoundCollisionShape;

    private int maxTextureMetadata;

    public AbstractProp(String packName, String fileName) {
        super(packName, fileName);
        itemScale = 0.3f;
    }

    @Override
    public void markFailedShape() {
        //DO STH ?
    }

    @Override
    public void generateShape() {
        compoundCollisionShape = new CompoundCollisionShape();
        if (getPartShapes().isEmpty()) {
            ObjModelPath modelPath = DynamXUtils.getModelPath(getPackName(), model);
            if (useHullShape) {
                compoundCollisionShape = ShapeUtils.generateComplexModelCollisions(DynamXUtils.getModelPath(getPackName(), model), "", scaleModifier, new Vector3f(), 0);
            } else {
                PackInfo info = DynamXObjectLoaders.PACKS.findPackInfoByPackName(getPackName());
                ShapeUtils.generateModelCollisions(this, DynamXContext.getObjModelDataFromCache(modelPath), compoundCollisionShape);
            }
        } else {
            getPartShapes().forEach(shape -> {
                getCollisionBoxes().add(shape.getBoundingBox().offset(0.5, 0.5, 0.5));
                switch (shape.getShapeType()) {
                    case BOX:
                        compoundCollisionShape.addChildShape(new BoxCollisionShape(shape.getSize()), shape.getPosition());
                        break;
                    case CYLINDER:
                        compoundCollisionShape.addChildShape(new CylinderCollisionShape(shape.getSize(), 0), shape.getPosition());
                        break;
                    case SPHERE:
                        compoundCollisionShape.addChildShape(new SphereCollisionShape(shape.getSize().x), shape.getPosition());
                        break;
                }
            });
        }
    }

    @Override
    public void onComplete(boolean hotReload) {
        textures.clear();
        textures.put((byte) 0, new TextureVariantData("default", (byte) 0, getName().toLowerCase()));
        if (texturesArray != null) {
            byte id = 1;
            for (String[] info : texturesArray) {
                textures.put(id, new TextureVariantData(info[0].toLowerCase(), id, info[1] == null ? "dummy" : info[1].toLowerCase()));
                id++;
            }
        }
        int texCount = 0;
        for (TextureVariantData data : textures.values()) {
            if (data.isItem())
                texCount++;
        }
        this.maxTextureMetadata = texCount;
    }

    @Nullable
    @Override
    public Map<Byte, TextureVariantData> getTextureVariantsFor(ObjObjectRenderer objObjectRenderer) {
        return textures;
    }

    public int getMaxTextureMetadata() {
        return maxTextureMetadata;
    }

    @Override
    public boolean hasVaryingTextures() {
        return textures.size() > 1;
    }

    @Override
    public Vector3f getScaleModifier() {
        return scaleModifier;
    }

    public void setScaleModifier(Vector3f scale) {
        this.scaleModifier = scale;
    }

    @Override
    public void addPart(BasePart<?> tBasePart) {
        parts.add(tBasePart);
    }

    @Override
    public void addCollisionShape(PartShape<?> partShape) {
        partShapes.add(partShape);
    }

    public <A extends BasePart<?>> List<A> getPartsByType(Class<A> clazz) {
        return (List<A>) this.parts.stream().filter(p -> clazz.equals(p.getClass())).collect(Collectors.toList());
    }

    public List<BasePart<?>> getParts() {
        return parts;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    public float getRenderDistance() {
        return renderDistance;
    }

    public void setRenderDistance(float renderDistance) {
        this.renderDistance = renderDistance;
    }

    public boolean doesUseHullShape() {
        return useHullShape;
    }

    public CompoundCollisionShape getCompoundCollisionShape() {
        return compoundCollisionShape;
    }

    public List<MutableBoundingBox> getCollisionBoxes() {
        return collisionBoxes;
    }

    public List<PartShape<?>> getPartShapes() {
        return partShapes;
    }

    @Override
    public List<BasePart<?>> getAllParts() {
        return parts;
    }
}