package com.gsss;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private final Color bgcolor = Color.BLACK;
    private final float speed = 5.0f;
    private SpriteBatch batch;
    private FitViewport viewport;
    private Texture gram;
    private Texture whiteTexture;
    private Texture blackTexture;
    private Texture baseGramTexture;
    private FrameBuffer fbo;
    private List<Vector2> path;
    private int currentPathIndex;
    private Texture highlightTexture;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new FitViewport(32, 16);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            false);

        gram = new Texture("gramav2.png");
        gram.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        whiteTexture = createSolidColorTexture(Color.WHITE);
        blackTexture = createSolidColorTexture(Color.BLACK);
        highlightTexture = createSolidColorTexture(new Color(0, 0, 1, 0.5f));

        currentPathIndex = 0;
        makePathMove();
        generateBoardTexture();
    }

    @Override
    public void render() {
        ScreenUtils.clear(bgcolor);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        if (currentPathIndex < path.size() - 1) {
            Vector2 currentTarget = path.get(currentPathIndex + 1);
            Vector2 currentPosition = path.get(currentPathIndex);

            float distance = currentPosition.dst(currentTarget);
            float alpha = speed * Gdx.graphics.getDeltaTime() / distance;

            if (alpha >= 1) {
                // Chegou ao próximo ponto
                currentPathIndex++;
                alpha = 1;
            }

            // Interpolar entre os pontos
            Vector2 newPosition = interpolate(currentPosition, currentTarget, alpha);

            // Atualizar a posição no caminho
            path.set(currentPathIndex, newPosition);
        }

        // Obter a posição do mouse no mundo do jogo
        Vector2 mousePos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        // Determinar em qual quadrado o mouse está
        int mouseGridX = (int) mousePos.x;
        int mouseGridY = (int) mousePos.y;


        batch.begin();
        batch.draw(baseGramTexture, 0, 0, 32, 16, 0, 0, 1, 1);
        batch.draw(blackTexture, path.get(currentPathIndex).x, path.get(currentPathIndex).y, 1, 1);
        // Verificar se o mouse está sobre um quadrado válido
        if (mouseGridX >= 0 && mouseGridX < 32 && mouseGridY >= 0 && mouseGridY < 16) {
            // Desenhar o quadrado destacado
            batch.draw(highlightTexture, mouseGridX, mouseGridY, 1, 1);
        }


        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        gram.dispose();
        whiteTexture.dispose();
        blackTexture.dispose();
        baseGramTexture.dispose();
        fbo.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private Texture createSolidColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void generateBoardTexture() {
        fbo.begin();
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 16; j++) {
                batch.draw(gram, i, j, 1, 1);
//                // do a chess colors
//                if (i % 2 == 0 && j % 2 == 0) {
//                    batch.draw(gram, i, j, 1, 1);
//                } else if (i % 2 == 1 && j % 2 == 1) {
//                    batch.draw(gram, i, j, 1, 1);
//                }
            }
        }
        batch.end();
        fbo.end();

        baseGramTexture = fbo.getColorBufferTexture();
        baseGramTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private Vector2 interpolate(Vector2 start, Vector2 end, float alpha) {
        return new Vector2(start.x + (end.x - start.x) * alpha, start.y + (end.y - start.y) * alpha);
    }

    private void makePathMove() {
        path = new ArrayList<>();
        path.add(new Vector2(0, 4));
        path.add(new Vector2(6, 4));
        path.add(new Vector2(6, 11));
        path.add(new Vector2(27, 11));
        path.add(new Vector2(27, 4));
        path.add(new Vector2(31, 4));
    }
}
