/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.render;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.glDeleteProgram;
import static org.lwjgl.opengl.GL20C.glDeleteShader;
import static org.lwjgl.opengl.GL20C.glDetachShader;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
//import org.lwjgl.util.vector.Matrix4f;
//import org.lwjgl.util.vector.Vector3f;

/**
 * Designed to be the base class of a custom shader
 *
 * @author zipCoder933
 */
public class Shader {

    private int programID;
    private int vertexID;
    private int fragmentID;

    public void delete() {
        glDetachShader(getID(), vertexID);
        glDetachShader(getID(), fragmentID);
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
        glDeleteProgram(getID());
    }

    public Shader() {
    }

    public Shader(File Vert, File Frag) throws IOException {
        init(Vert, Frag);
    }

    public Shader(String Vert, String Frag) throws IOException {
        init(Vert, Frag);
    }

    public void init(File Vert, File Frag) throws IOException {
        vertexID = loadShader(Vert, GL20.GL_VERTEX_SHADER);
        fragmentID = loadShader(Frag, GL20.GL_FRAGMENT_SHADER);
        programID = GL20.glCreateProgram(); //this id is how opengl will know what shader group (program) to use
        GL20.glAttachShader(programID, vertexID); //link shaders to the program id
        GL20.glAttachShader(programID, fragmentID);

        bindAttributes(); //link components of the shader together

        GL20.glLinkProgram(programID); //finalization
        if (glGetProgrami(programID, GL_LINK_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }
        GL20.glValidateProgram(programID);
    }

    public void init(String Vert, String Frag) throws IOException {
        vertexID = loadShaderString(Vert, GL20.GL_VERTEX_SHADER);
        fragmentID = loadShaderString(Frag, GL20.GL_FRAGMENT_SHADER);
        programID = GL20.glCreateProgram(); //this id is how opengl will know what shader group (program) to use
        GL20.glAttachShader(programID, vertexID); //link shaders to the program id
        GL20.glAttachShader(programID, fragmentID);

        bindAttributes(); //link components of the shader together

        GL20.glLinkProgram(programID); //finalization
        if (glGetProgrami(programID, GL_LINK_STATUS) != GL_TRUE) {
            throw new IllegalStateException();
        }
        GL20.glValidateProgram(programID);
    }

    /**
     * What these methods do is they get the Uniform Variable Location and
     * Attribute Variable Location of variables in the GLSL shader. A Uniform
     * Variable is a per-primitive parameter that is used during the whole
     * drawing call, whereas a Attribute Variable is per-vertex, and refers to
     * the UV, Color, Positions.
     */
    /**
     * get the id of a uniform by entering its name
     *
     * @param uniformName
     * @return the uniform id
     */
    public int getUniformLocation(String uniformName) {
        GL20.glUseProgram(programID);
        return GL20.glGetUniformLocation(programID, uniformName);
    }

    public void bindAttribute(int attribute, String variableName) {
        GL20.glUseProgram(programID);
        GL20.glBindAttribLocation(programID, attribute, variableName);
    }



//<editor-fold defaultstate="collapsed" desc="sending uniforms">
    /**
     * Alternative way to send uniforms is like so:
     * glUseProgram(shader.getID()); glUniform2f(windowUniform, width, height);
     * <br> Note that we cannot do anything to a shader without calling
     * glUseProgram on it first to bind it
     */
    /**
     * send a float to the shader as a uniform
     *
     * @param location the uniform id
     * @param value the actual value to send
     */
    public void loadFloat(int location, float value) {
//        GL20.glUniform1f(location, value);
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniform1fARB(location, value);
    }

    public void loadInt(int location, int value) {
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniform1iARB(location, value);
    }

    public void loadVec3f(int location, Vector3f vector) {
//        GL20.glUniform3f(location, vector.x, vector.y, vector.z);
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniform3fARB(location, vector.x, vector.y, vector.z);
    }

    public void loadVec4f(int location, Vector4f vector) {
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniform4fARB(location, vector.x, vector.y, vector.z, vector.w);
    }

    public void loadVec2f(int location, Vector2f vector) {
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniform2fARB(location, vector.x, vector.y);
    }

    public void loadVec3i(int location, Vector3i vector) {
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniform3iARB(location, vector.x, vector.y, vector.z);
    }

    public void loadVec2i(int location, Vector2i vector) {
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniform2iARB(location, vector.x, vector.y);
    }

    /**
     * Loads a 4x4 matrix to the shader. You can load a JOML matrix into the
     * shader like so:<br><br>
     * <code>
     * FloatBuffer buffer = BufferUtils.createFloatBuffer(16);<br>
     * matrix.get(buffer);
     * </code>
     * <br><br>
     * (matrix.get(buffer) in JOML (Java OpenGL Math Library) copies the matrix
     * elements into the provided FloatBuffer )
     *
     * @param location the uniform ID
     * @param transpose transpose the matrix
     * @param buffer the matrix buffer
     */
    public void loadMatrix4f(int location, FloatBuffer buffer) {
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniformMatrix4fvARB(location, false, buffer);
    }

    /**
     * Loads a 4x4 matrix to the shader. You can load a JOML matrix into the
     * shader like so:<br><br>
     * <code>
     * FloatBuffer buffer = BufferUtils.createFloatBuffer(16);<br>
     * matrix.get(buffer);
     * </code>
     * <br><br>
     * (matrix.get(buffer) in JOML (Java OpenGL Math Library) copies the matrix
     * elements into the provided FloatBuffer )
     *
     * @param location the uniform ID
     * @param transpose transpose the matrix
     * @param buffer the matrix buffer
     */
    public void loadMatrix4f(int location, boolean transpose, FloatBuffer buffer) {
//        GL20.glUniformMatrix4fv(location, false, buffer);
        ARBShaderObjects.glUseProgramObjectARB(getID());
        ARBShaderObjects.glUniformMatrix4fvARB(location, transpose, buffer);
    }
//</editor-fold>

    public void bind() {//use shader
        GL20.glUseProgram(programID);
    }

    public static void unbind() {//reset to default render mode
        GL20.glUseProgram(0);
    }

    public void bindAttributes(){}

    private static int loadShaderString(String shaderSource, int type) throws IOException {
        int ID = GL20.glCreateShader(type); //create shader and assign an ID to it. the typeReference tells us what typeReference of shader we want (vert,frag,etc)
        GL20.glShaderSource(ID, shaderSource);
        GL20.glCompileShader(ID); //compile the shader, and create an id for it

        if (GL20.glGetShaderi(ID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) { //get status of the compiled shader. if it has erros, print out error messages
            throw new IOException("Couldn't compile shader:\n"
                    + GL20.glGetShaderInfoLog(ID, 512));
        }
        return ID;
    }

    private static int loadShader(File file, int type) throws IOException {
        StringBuilder shaderSource = new StringBuilder();
        try {//load the shader text
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            throw new IOException("Can't read shader file \"" + file + "\"", e);
        }
        int ID = GL20.glCreateShader(type); //create shader and assign an ID to it. the typeReference tells us what typeReference of shader we want (vert,frag,etc)
        GL20.glShaderSource(ID, shaderSource);
        GL20.glCompileShader(ID); //compile the shader, and create an id for it

        if (GL20.glGetShaderi(ID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) { //get status of the compiled shader. if it has erros, print out error messages
            throw new IOException("Couldn't compile shader \"" + file + "\":\n"
                    + GL20.glGetShaderInfoLog(ID, 512));
        }
        return ID;
    }

    public int getID() {
        return programID;
    }
}
