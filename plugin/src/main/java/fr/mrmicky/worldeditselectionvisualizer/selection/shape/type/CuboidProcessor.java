package fr.mrmicky.worldeditselectionvisualizer.selection.shape.type;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import fr.mrmicky.worldeditselectionvisualizer.WorldEditSelectionVisualizer;
import fr.mrmicky.worldeditselectionvisualizer.compat.RegionAdapter;
import fr.mrmicky.worldeditselectionvisualizer.config.GlobalSelectionConfig;
import fr.mrmicky.worldeditselectionvisualizer.math.Vector3d;
import fr.mrmicky.worldeditselectionvisualizer.selection.SelectionPoints;
import fr.mrmicky.worldeditselectionvisualizer.selection.shape.ShapeProcessor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CuboidProcessor extends ShapeProcessor<CuboidRegion> {

    public CuboidProcessor(WorldEditSelectionVisualizer plugin) {
        super(CuboidRegion.class, plugin);
    }

    @Override
    public void processSelection(SelectionPoints selection, CuboidRegion region, RegionAdapter regionAdapter, GlobalSelectionConfig config) {
        Vector3d pos1 = regionAdapter.getPos1();
        Vector3d pos2 = regionAdapter.getPos2();
        if (pos1 != Vector3d.ZERO)
            selection.primaryPositions().add(pos1);
        if (pos2 != Vector3d.ZERO)
            selection.setSecondaryPosition(pos2);
        if (pos1 == Vector3d.ZERO || pos2 == Vector3d.ZERO)
            // incomplete selection, can't draw lines.
            return;

        Vector3d min = regionAdapter.getMinimumPoint();
        Vector3d max = regionAdapter.getMaximumPoint().add(1, 1, 1);
        int height = region.getHeight();
        int width = region.getWidth();
        int length = region.getLength();

        List<Vector3d> bottomCorners = new ArrayList<>(4);
        bottomCorners.add(min);
        bottomCorners.add(min.withX(max.getX()));
        bottomCorners.add(max.withY(min.getY()));
        bottomCorners.add(min.withZ(max.getZ()));

        createLinesFromBottom(selection, bottomCorners, height, config);

        double lineGap = config.secondary().getLinesGap();
        double distance = config.secondary().getPointsDistance();

        if (lineGap > 0 && getPlugin().getConfig().getBoolean("cuboid-top-bottom")) {
            for (double offset = lineGap; offset < width; offset += lineGap) {
                createLine(selection.secondary(), min.add(offset, 0, 0), min.add(offset, 0, length), distance);
                createLine(selection.secondary(), min.add(offset, height, 0), min.add(offset, height, length), distance);
            }
        }
    }
}
