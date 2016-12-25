package conway3d;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

/**
 * @author Connor Denman
 *         <p>
 *         This is the entry point class for Conway's Game of Life in 3D.
 */

public class Main extends Application
{
  private int w, h, depth;
  private static final int GRID_SIZE = 30;
  private static final float RANDOM_DENSITY = 0.05f;
  private static final int BOX_SIZE = 1;
  private int[][][][] world;
  private Cell[][][] cells;

  private static final int[] INIT_RULES = {4, 10, 10, 4};
  private static final String[] PRESET_NAMES = {
          "Random", "Two Walls", "Weird Corners", "Halfsies", "Triangular", "Single Beam"
  };
  private Slider[] rSliders;

  private Group root;
  private SubScene subScene;
  final Xform cubeGroup = new Xform();
  final Xform worldForm = new Xform();
  final PerspectiveCamera camera = new PerspectiveCamera(true);
  final Xform cameraXform = new Xform();
  final Xform cameraXform2 = new Xform();
  final Xform cameraXform3 = new Xform();
  private static final double CAMERA_INITIAL_DISTANCE = -450;
  private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
  private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
  private static final double CAMERA_NEAR_CLIP = 0.1;
  private static final double CAMERA_FAR_CLIP = 10000.0;
  private static final double CONTROL_MULTIPLIER = 0.1;
  private static final double SHIFT_MULTIPLIER = 10.0;
  private static final double MOUSE_SPEED = 0.1;
  private static final double ROTATION_SPEED = 2.0;
  private static final double TRACK_SPEED = 0.3;

  double mousePosX;
  double mousePosY;
  double mouseOldX;
  double mouseOldY;
  double mouseDeltaX;
  double mouseDeltaY;

  /**
   * Prepare the JavaFX camera.
   *
   * @return Nothing
   */
  private void buildCamera()
  {
    root.getChildren().add(cameraXform);
    cameraXform.getChildren().add(cameraXform2);
    cameraXform2.getChildren().add(cameraXform3);
    cameraXform3.getChildren().add(camera);
    cameraXform3.setRotateZ(180.0);

    camera.setNearClip(CAMERA_NEAR_CLIP);
    camera.setFarClip(CAMERA_FAR_CLIP);
    camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
    cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
    cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
  }

  /**
   * Handle for when the user uses the mouse to rotate the molecule being displayed.
   *
   * @param scene - the scene where the action applies.
   * @param root  - root node.
   */
  private void handleMouse(Scene scene, final Node root)
  {
    scene.setOnMousePressed(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent me)
      {
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseOldX = me.getSceneX();
        mouseOldY = me.getSceneY();
      }
    });
    scene.setOnMouseDragged(new EventHandler<MouseEvent>()
    {
      @Override
      public void handle(MouseEvent me)
      {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);

        double modifier = 1.0;

        if (me.isControlDown())
        {
          modifier = CONTROL_MULTIPLIER;
        }

        if (me.isShiftDown())
        {
          modifier = SHIFT_MULTIPLIER;
        }
        if (me.isPrimaryButtonDown())
        {
          cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
          cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
        } else if (me.isSecondaryButtonDown())
        {
          double z = camera.getTranslateZ();
          double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
          camera.setTranslateZ(newZ);
        } else if (me.isMiddleButtonDown())
        {
          cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
          cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
        }
      }
    });
  }


  /**
   * Prepare the game of life 3D grid and data structure.
   *
   * @param gridSize - e.g. "30" for a 30x30x30 grid.
   * @param preset   - the integer representation of the desired grid preset.
   */
  private void setupGame(int preset)
  {
    cubeGroup.getChildren().clear();
    w = GRID_SIZE;
    h = GRID_SIZE;
    depth = GRID_SIZE;
    world = new int[w][h][depth][2];
    cells = new Cell[w][h][depth];
    Random rand = new Random();
    //    different grid presets
    switch (preset)
    {
      case 0:
        // random preset
        for (int i = 0; i < RANDOM_DENSITY * w * h * depth; i += BOX_SIZE)
        {
          world[rand.nextInt(w)][rand.nextInt(h)][rand.nextInt(depth)][1] = 1;
        }

        break;
      case 1:
        // 2 walls of cells on opposite sides of the cube
        for (int j = 0; j < w; j += BOX_SIZE)
        {
          for (int k = 0; k < h; k += BOX_SIZE)
          {
            world[0][j][k][1] = 1;
            world[depth - 1][j][k][1] = 1;
          }
        }

        break;
      case 2:
        // weird corners
        for (int i = 0; i < rand.nextInt(w); i += BOX_SIZE)
        {
          for (int j = 0; j < rand.nextInt(h); j += BOX_SIZE)
          {
            for (int k = 0; k < rand.nextInt(depth); k += BOX_SIZE)
            {
              world[i][j][k][1] = 1;
            }
          }
        }

        break;
      case 3:
        // half of all axes
        for (int i = 0; i < w/2; i += BOX_SIZE)
        {
          for (int j = 0; j < h/2; j += BOX_SIZE)
          {
            for (int k = 0; k < depth/2; k += BOX_SIZE)
            {
              world[i][j][k][1] = 1;
            }
          }
        }

        break;
      case 4:
        for (int i = 0; i < w; i += BOX_SIZE)
        {
          for (int j = 0; j < i; j += BOX_SIZE)
          {
            for (int k = 0; k < j; k += BOX_SIZE)
            {
              world[i][j][k][1] = 1;
            }
          }
        }
        break;
      case 5:
        for (int i = 0; i < w; i += BOX_SIZE)
        {
          world[i][i][i][1] = 1;
        }
        break;
    }
  }


  @Override
  /**
   * Overridden start method, takes a Stage element
   *
   * Sets up the JavaFX scene for drawing and calls functions
   * for building the camera, axes, and molecule.
   *
   * @param primaryStage - the main stage.
   */
  public void start(Stage primaryStage)
  {
    root = new Group();

    buildCamera();

    subScene = new SubScene(root, 1024, 768, false, SceneAntialiasing.DISABLED);
    subScene.setFill(Color.LIGHTGREY);
    subScene.setCamera(camera);

    // 2D
    BorderPane pane = new BorderPane();
    pane.setCenter(subScene);

//    UI controls
    ToolBar toolBar = new ToolBar(new Label("Presets"));
    rSliders = new Slider[4];

//    setup preset buttons
    for (int i = 0; i < PRESET_NAMES.length; i++)
    {
      Button currentButton = new Button(PRESET_NAMES[i]);
      final int currentIndex = i;
      currentButton.setOnAction((event) ->
      {
        setupGame(currentIndex);
      });

      toolBar.getItems().add(currentButton);
    }

    toolBar.getItems().add(new Label("Rules"));

    //    setup rule input sliders
    for (int i = 0; i < 4; i++)
    {
      rSliders[i] = new Slider();
      rSliders[i].setMin(0);
      rSliders[i].setMax(26);
      rSliders[i].setValue(INIT_RULES[i]);
      rSliders[i].setShowTickLabels(true);
      rSliders[i].setShowTickMarks(true);

      toolBar.getItems().addAll(new Label("r" + Integer.toString(i + 1)), rSliders[i]);
    }

    toolBar.setOrientation(Orientation.VERTICAL);
    pane.setRight(toolBar);
    pane.setPrefSize(300, 300);

    Scene scene = new Scene(pane);

    worldForm.getChildren().add(cubeGroup);
    root.getChildren().add(worldForm);

    handleMouse(scene, worldForm);

    scene.setOnScroll(new EventHandler<ScrollEvent>()
    {
      @Override
      public void handle(ScrollEvent event)
      {
        double zoomFactor = 1.05;
        double deltaY = event.getDeltaY();
        if (deltaY < 0)
        {
          zoomFactor = 2.0 - zoomFactor;
        }
        System.out.println(zoomFactor);
        worldForm.setScaleX(worldForm.getScaleX() * zoomFactor);
        worldForm.setScaleY(worldForm.getScaleY() * zoomFactor);
        worldForm.setScaleZ(worldForm.getScaleZ() * zoomFactor);
        event.consume();
      }
    });

    primaryStage.setScene(scene);
    primaryStage.setTitle("Conway's GOL 3D - Connor Denman");
    primaryStage.show();

    setupGame(0);
    new GameLoop().start();
  }

  /**
   * @author Connor Denman
   *         <p>
   *         GameLoop handles the primary game animation frame timing.
   */
  class GameLoop extends AnimationTimer
  {

    private long lastUpdate = 0;

    @Override
    public void handle(long now)
    {

//      wait 1 second to update the cells
      if (now - lastUpdate < 1_000_000_000)
      {
        cubeGroup.setRotate(cubeGroup.getRotate() + 0.5f);
        return;
      }

      lastUpdate = now;

      for (int i = 0; i < w; i += BOX_SIZE)
      {
        for (int j = 0; j < h; j += BOX_SIZE)
        {
          for (int k = 0; k < depth; k += BOX_SIZE)
          {
            if ((world[i][j][k][1] == 1))
            {
              world[i][j][k][0] = 1;

              Cell newBox = new Cell();
              newBox.setTranslateX(i);
              newBox.setTranslateY(j);
              newBox.setTranslateZ(k);

              cubeGroup.getChildren().add(newBox);
              newBox.animateSpawn();
              cells[i][j][k] = newBox;
            }
            if (world[i][j][k][1] == -1)
            {
              world[i][j][k][0] = 0;
              cells[i][j][k].animateDeath();
            }

            world[i][j][k][1] = 0;
          }
        }
      }


      for (int i = 0; i < w; i += BOX_SIZE)
      {
        for (int j = 0; j < h; j += BOX_SIZE)
        {
          for (int k = 0; k < depth; k += BOX_SIZE)
          {
            int count = neighbors(i, j, k);
            if ((count >= (int) rSliders[0].getValue()) && (count <= (int) rSliders[1].getValue()) && world[i][j][k][0] == 0)
            {
              world[i][j][k][1] = 1;
            }

            if ((count > (int) rSliders[2].getValue() || count < (int) rSliders[3].getValue()) && world[i][j][k][0] == 1)
            {
              world[i][j][k][1] = -1;
            }
          }
        }
      }
    }

    /**
     * Count all neighbors of cell specified at x, y, z.
     *
     * @param x - x value of currently selected cell.
     * @param y - y value of currently selected cell.
     * @param z - z value of currently selected cell.
     * @return Total number of neighbors of selected cell.
     */
    private int neighbors(int x, int y, int z)
    {
      return world[(x + BOX_SIZE) % w][y][z][0] +
              world[x][(y + BOX_SIZE) % h][z][0] +
              world[(x + w - BOX_SIZE) % w][y][z][0] +
              world[x][(y + h - BOX_SIZE) % h][z][0] +
              world[(x + BOX_SIZE) % w][(y + BOX_SIZE) % h][z][0] +
              world[(x + w - BOX_SIZE) % w][(y + BOX_SIZE) % h][z][0] +
              world[(x + BOX_SIZE) % w][(y + h - BOX_SIZE) % h][z][0] +
              world[(x + w - BOX_SIZE) % w][(y + h - BOX_SIZE) % h][z][0] +
              world[(x + BOX_SIZE) % w][y][(z + BOX_SIZE) % depth][0] +
              world[x][(y + BOX_SIZE) % h][(z + BOX_SIZE) % depth][0] +
              world[(x + w - BOX_SIZE) % w][y][(z + BOX_SIZE) % depth][0] +
              world[x][(y + h - BOX_SIZE) % h][(z + BOX_SIZE) % depth][0] +
              world[(x + BOX_SIZE) % w][(y + BOX_SIZE) % h][(z + BOX_SIZE) % depth][0] +
              world[(x + w - BOX_SIZE) % w][(y + BOX_SIZE) % h][(z + BOX_SIZE) % depth][0] +
              world[(x + BOX_SIZE) % w][(y + h - BOX_SIZE) % h][(z + BOX_SIZE) % depth][0] +
              world[(x + w - BOX_SIZE) % w][(y + h - BOX_SIZE) % h][(z + BOX_SIZE) % depth][0] +
              world[(x + BOX_SIZE) % w][y][(z + depth - BOX_SIZE) % depth][0] +
              world[x][(y + BOX_SIZE) % h][(z + depth - BOX_SIZE) % depth][0] +
              world[(x + w - BOX_SIZE) % w][y][(z + depth - BOX_SIZE) % depth][0] +
              world[x][(y + h - BOX_SIZE) % h][(z + depth - BOX_SIZE) % depth][0] +
              world[(x + BOX_SIZE) % w][(y + BOX_SIZE) % h][(z + depth - BOX_SIZE) % depth][0] +
              world[(x + w - BOX_SIZE) % w][(y + BOX_SIZE) % h][(z + depth - BOX_SIZE) % depth][0] +
              world[(x + BOX_SIZE) % w][(y + h - BOX_SIZE) % h][(z + depth - BOX_SIZE) % depth][0] +
              world[(x + w - BOX_SIZE) % w][(y + h - BOX_SIZE) % h][(z + depth - BOX_SIZE) % depth][0];
    }
  }

  /**
   * The main() method is ignored in correctly deployed JavaFX application.
   * main() serves only as fallback in case the application can not be
   * launched through deployment artifacts, e.g., in IDEs with limited FX
   * support. NetBeans ignores main().
   *
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    launch(args);
  }
}
