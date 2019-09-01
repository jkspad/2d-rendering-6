package com.jkspad.tutorial;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author John Knight
 * Copyright http://www.jkspad.com
 *
 */
public class TexturedQuad extends ApplicationAdapter implements InputProcessor {
	private ShaderProgram shader;
	private Texture texture;
	private Mesh mesh;
	private Mesh mesh1;
	private SpriteBatch spriteBatch;
	private BitmapFont font;

	private static final int TEXT_X = 10;
	private static final int TEXT_Y = 20;

	private enum State{
		Nearest,
		Linear,
		Repeat,
		RepeatMirror,
		ClampToEdge,
		Mixed,
		End;

		public State next() {
			State next = values()[ordinal() + 1];
			if(next == End){
				return values()[0];
			}
			return next;
		}
	}

	private State state = State.Nearest;

	private final String VERTEX_SHADER =
			"attribute vec4 a_position; \n" +
					"attribute vec2 a_texCoord; \n" +
					"varying vec2 v_texCoord; \n" +
					"void main() \n" + "{ \n" +
					" gl_Position = a_position; \n" +
					" v_texCoord = a_texCoord; \n" +
					"} \n";

	private final String FRAGMENT_SHADER =
			"#ifdef GL_ES\n" +
					"precision mediump float;\n" +
					"#endif\n"+
					"varying vec2 v_texCoord; \n" +
					"uniform sampler2D u_texture; \n" +
					"void main() \n"+
					"{ \n"+
					" gl_FragColor = texture2D( u_texture, v_texCoord );\n"+
					"} \n";

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();

		mesh = new Mesh(true, 4, 0, new VertexAttribute(Usage.Position, 2, "a_position"), new VertexAttribute(
				Usage.TextureCoordinates, 2, "a_texCoord"));
		mesh1 = new Mesh(true, 4, 0, new VertexAttribute(Usage.Position, 2, "a_position"), new VertexAttribute(
				Usage.TextureCoordinates, 2, "a_texCoord"));

		{
			float[] vertices = {
					-0.5f, -0.5f,	// quad bottom left
					0.0f, 1.0f, 	// texture bottom left
					0.5f, -0.5f, 	// quad bottom right
					1f, 1.0f, 	    // texture bottom right
					-0.5f, 0.5f,	// quad top left
					0.0f, 0.0f, 	// texture top left
					0.5f, 0.5f,		// quad top right
					1.0f, 0.0f 		// texture top-right
			};
			mesh.setVertices(vertices);
		}

		{
			float[] vertices = {
					-0.5f, -0.5f,	// quad bottom left
					0.0f, 2.0f, 	// texture bottom left
					0.5f, -0.5f, 	// quad bottom right
					4.0f, 2.0f, 	// texture bottom right
					-0.5f, 0.5f,	// quad top left
					0.0f, 0.0f, 	// texture top left
					0.5f, 0.5f,		// quad top right
					4.0f, 0.0f 		// texture top-right
			};
			mesh1.setVertices(vertices);

		}

		createTexture();
		createMeshShader();
		font = new BitmapFont();
		Gdx.input.setInputProcessor(this);
	}


	protected void createMeshShader() {
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
		String log = shader.getLog();
		if (!shader.isCompiled()){
			throw new GdxRuntimeException(log);
		}
		if (log!=null && log.length()!=0){
			Gdx.app.log("shader log", log);
		}
	}


	private void createTexture () {
		texture = new Texture("bob.png");
	}

	private void showMessage(){
		spriteBatch.begin();
		font.draw(spriteBatch, "Hit space baby", TEXT_X, TEXT_Y);
		spriteBatch.end();
	}

	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		switch(state){
			case Nearest:
				texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
				break;
			case Linear:
				texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				break;
			case Repeat:
				texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
				texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				break;
			case RepeatMirror:
				texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
				texture.setWrap(TextureWrap.MirroredRepeat, TextureWrap.MirroredRepeat);
				break;
			case ClampToEdge:
				texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
				texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
				break;
			case Mixed:
				texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
				texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.Repeat);
				break;
			default:
				break;
		}

		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);

		texture.bind();
		shader.begin();
		shader.setUniformi("u_texture", 0);

		switch(state){
			case Nearest:
			case Linear:
				mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
				break;
			case Repeat:
			case RepeatMirror:
			case ClampToEdge:
			case Mixed:
				mesh1.render(shader, GL20.GL_TRIANGLE_STRIP);
				break;
			default:
				break;
		}

		shader.end();
		showMessage();
	}

	@Override
	public void dispose () {
		texture.dispose();
		shader.dispose();
		mesh.dispose();
	}


	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Keys.SPACE){
			state = state.next();
			return true;
		}
		return false;
	}


	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}

