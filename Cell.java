package conway3d;

import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;

/**
 * @author Connor Denman
 *         <p>
 *         This class maintains the visual state of a cell in Conway's 3D game of life.
 *         Subclass of JavaFX 3D Box.
 */
public class Cell extends Box
{
  private static final PhongMaterial blueMaterial = new PhongMaterial(Color.LIGHTBLUE);
  private static final PhongMaterial redMaterial = new PhongMaterial(Color.RED);
  private static final PhongMaterial greenMaterial = new PhongMaterial(Color.LIGHTGREEN);

  /**
   * Constructor of Cell. Simply sets the size of the box and the initial material (green).
   *
   * @return Nothing
   */
  public Cell()
  {
    super(1, 1, 1);
    setMaterial(greenMaterial);
  }

  /**
   * Performs the scaling animation of the cell when it initially spawns, and changes the color of the cell.
   *
   * @return Nothing
   */
  public void animateSpawn()
  {
    this.setScaleX(0.0f);
    this.setScaleY(0.0f);
    this.setScaleZ(0.0f);

    ScaleTransition st = new ScaleTransition(Duration.millis(300), this);
    st.setToX(1.0f);
    st.setToY(1.0f);
    st.setToZ(1.0f);
    st.setCycleCount(1);
    st.setAutoReverse(false);
    st.play();

    st.setOnFinished(event ->
    {
      this.setMaterial(blueMaterial);
    });
  }

  /**
   * Performs the de-scaling animation of the cell when it is dying, and changes the color.
   *
   * @return Nothing
   */
  public void animateDeath()
  {
    setMaterial(redMaterial);

    ScaleTransition st = new ScaleTransition(Duration.millis(300), this);
    st.setToX(0.0f);
    st.setToY(0.0f);
    st.setToZ(0.0f);
    st.setCycleCount(1);
    st.setAutoReverse(false);
    st.play();

    st.setOnFinished(event ->
    {
      Group parent = (Group) this.getParent();
      parent.getChildren().remove(this);
    });
  }
}
