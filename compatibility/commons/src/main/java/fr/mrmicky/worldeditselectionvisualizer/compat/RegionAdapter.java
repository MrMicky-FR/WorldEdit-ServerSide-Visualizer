package fr.mrmicky.worldeditselectionvisualizer.compat;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import fr.mrmicky.worldeditselectionvisualizer.math.Vector3d;
import fr.mrmicky.worldeditselectionvisualizer.selection.RegionInfos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RegionAdapter {

    @NotNull
    Vector3d getMinimumPoint();

    @NotNull
    Vector3d getMaximumPoint();

    Vector3d getPos1();
    Vector3d getPos2();

    @NotNull
    Vector3d getCenter();

    @NotNull
    List<Vector3d> getPolygonalPoints();

    @NotNull
    List<Vector3d> getPolyhedralVertices();

    @NotNull
    Vector3d getEllipsoidRadius();

    @NotNull
    default List<Vector3d[]> getConvexTriangles() {
        return getConvexTriangles(false);
    }

    @NotNull
    List<Vector3d[]> getConvexTriangles(boolean faweSupport);

    @NotNull
    Region transform(Transform transform, Vector3d origin);

    void shift(Vector3d vector) throws RegionOperationException;

    @NotNull
    Region getRegion();

    @NotNull
    default RegionInfos getRegionsInfos() {
        return new RegionInfos(this);
    }
}
