import com.almasb.fxgl.animation.Animation;
import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import com.almasb.fxgl.entity.component.Component;

public class Game extends GameApplication {

    private Entity player1, player2;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Simple Fighting Game");
        settings.setWidth(800);
        settings.setHeight(600);
    }

    public class JumpControl extends Component {
        private boolean isJumping;
        private double jumpHeight;

        public JumpControl(double jumpHeight) {
            this.jumpHeight = jumpHeight;
            this.isJumping = false;
        }

        public boolean isJumping() {
            return isJumping;
        }

        public void startJump() {
            if (!isJumping) {
                isJumping = true;
                jumpUp();
            }
        }

        private void jumpUp() {
            double startY = entity.getY();

            FXGL.animationBuilder()
                    .duration(Duration.seconds(0.4))
                    .onFinished(() -> isJumping = false)
                    .translate(entity)
                    .from(new Point2D(entity.getX(), startY))
                    .to(new Point2D(entity.getX(), startY - jumpHeight))
                    .buildAndPlay();
        }
    }


    @Override
    protected void initGame() {
        player1 = FXGL.entityBuilder()
                .at(100, 300)
                .viewWithBBox("player1.png")
                .with(new JumpControl(100))
                .buildAndAttach();

        player2 = FXGL.entityBuilder()
                .at(700, 300)
                .viewWithBBox("player2.png")
                .with(new JumpControl(100))
                .buildAndAttach();
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();

        input.addAction(new UserAction("Player 1 Move Left") {
            @Override
            protected void onAction() {
                player1.translateX(-5);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Player 1 Move Right") {
            @Override
            protected void onAction() {
                player1.translateX(5);
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Player 1 Jump") {
            @Override
            protected void onActionBegin() {
                player1.getComponent(JumpControl.class).startJump();
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Player 1 Duck") {
            @Override
            protected void onActionBegin() {
                player1.translateY(50);
            }

            @Override
            protected void onActionEnd() {
                player1.translateY(-50);
            }
        }, KeyCode.S);

        input.addAction(new UserAction("Player 2 Move Left") {
            @Override
            protected void onAction() {
                player2.translateX(-5);
            }
        }, KeyCode.LEFT);

        input.addAction(new UserAction("Player 2 Move Right") {
            @Override
            protected void onAction() {
                player2.translateX(5);
            }
        }, KeyCode.RIGHT);


        input.addAction(new UserAction("Player 2 Jump") {
            @Override
            protected void onActionBegin() {
                player2.getComponent(JumpControl.class).startJump();
            }
        }, KeyCode.UP);

        input.addAction(new UserAction("Player 2 Duck") {
            @Override
            protected void onActionBegin() {
                player2.translateY(50);
            }

            @Override
            protected void onActionEnd() {
                player2.translateY(-50);
            }
        }, KeyCode.DOWN);
    }

    private void onFallFinished(Entity player) {
        player.getProperties().setValue("isJumping", false);
    }

    private void jump(Entity player) {
        if (player.getProperties().getBoolean("isJumping")) {
            return;
        }

        player.getProperties().setValue("isJumping", true);
        double startY = player.getY();
        double jumpHeight = 100;

        Animation<?> jumpAnimation = FXGL.animationBuilder()
                .duration(Duration.seconds(0.4))
                .interpolator(Interpolators.EXPONENTIAL.EASE_OUT())
                .translate(player)
                .from(player.getPosition())
                .to(player.getPosition().subtract(0, jumpHeight))
                .build();

        Animation<?> fallAnimation = FXGL.animationBuilder()
                .duration(Duration.seconds(0.4))
                .interpolator(Interpolators.EXPONENTIAL.EASE_IN())
                .translate(player)
                .from(player.getPosition().subtract(0, jumpHeight))
                .to(new Point2D(player.getX(), startY))
                .build();

        jumpAnimation.setOnFinished(() -> {
            fallAnimation.setOnFinished(() -> onFallFinished(player));
            fallAnimation.start();
        });

        jumpAnimation.start();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

