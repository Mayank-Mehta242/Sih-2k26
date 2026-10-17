package Land.Slide.Early.Warn.system.LandSlidebyMayank.Error;

public class RoadSegmentNotFoundException extends RuntimeException {

    public RoadSegmentNotFoundException(Long id) {
        super("Road segment " + id + " was not found.");
    }
}
