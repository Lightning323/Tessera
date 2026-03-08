/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.obj;

import com.tessera.window.utils.obj.OBJ.Face;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector3f;

/*
Here's an outline of the process for combining multiple OBJ index attributes 
into a single index list:

1. **Initialize a `HashMap` for Unique Vertices**:
   - Create a `HashMap` to keep track of unique combinations of position attributes 
    (e.g., position, texture coordinates, normals).

2. **Iterate Over Faces**:
   - For each face in the OBJ file:
     - For each position in the face:
       - Get the position index and UV index.

3. **Create a New Vertex Object**:
   - Create a new `Vertex` object and set its attributes (position, texture 
    coordinates, normals, etc.) based on the indices obtained in step 2.

4. **Check if the Vertex Already Exists in `uniqueVertices`**:
   - Use the `Vertex` object as the key to check if it already exists in the `HashMap`.

5. **If Vertex is New, Add it to `uniqueVertices`**:
   - If the `Vertex` is not found in `uniqueVertices`, add it as a new 
    entry with a unique index.

6. **Populate the `indices` Array**:
   - For each position in the face:
     - Find the index of the `Vertex` in `uniqueVertices`.
     - Store this index in the `indices` array.

7. **Populate Vertex Attribute Buffers (e.g., `vertBuffer`, `uvBuffer`)**:
   - Iterate over the unique vertices in `uniqueVertices`.
   - For each unique position:
     - Populate the position attribute buffers with the corresponding components 
       (e.g., position, texture coordinates).

8. **End of Process**:
   - At this point, you have a single index list and position attribute buffers 
    (e.g., `vertBuffer`, `uvBuffer`) ready for rendering.

This process ensures that you have a single set of vertices with unique combinations of
attributes, which is essential for efficient rendering in OpenGL. It also 
eliminates redundancy and improves memory efficiency.
 */

public class IndexedOBJ {

    public int[] indices;
    public List<Vector3f> indexedVertices;
    public List<Vector2f> indexedTextureCoords;
    public List<Vector3f> indexedNormals;

    class Vertex {

        public Vertex(Vector3f vertex, Vector2f uv, Vector3f normals) {
            this.position = vertex;
            this.uv = uv;
            this.normals = normals;
        }
        Vector3f position;
        Vector2f uv;
        Vector3f normals;
    }

    // Constructor
    public IndexedOBJ(OBJ obj) {
        Map<Vertex, Integer> uniqueVertices = new HashMap<>();
        List<Integer> indicesList = new ArrayList<>();
        indexedVertices = new ArrayList<>();
        indexedTextureCoords = new ArrayList<>();
        indexedNormals = new ArrayList<>();

        for (Face face : obj.getFaces()) {
            for (int i = 0; i < face.getVertexCoordinates().length; i++) {

                int vertexIndex = face.getVertexCoordinates()[i];
                int textureIndex = face.getTextureCoords()[i];
                int normalIndex = face.getNormals()[i];

                Vertex newVertex = new Vertex(
                        obj.getVertexCoordinates().get(vertexIndex - 1),
                        obj.getTextureCoordinates().get(textureIndex - 1),
                        obj.getNormals().get(normalIndex - 1)
                );

                if (!uniqueVertices.containsKey(newVertex)) {
                    uniqueVertices.put(newVertex, indexedVertices.size());
                    indexedVertices.add(newVertex.position);
                    indexedTextureCoords.add(newVertex.uv);
                    indexedNormals.add(newVertex.normals);
                }

                indicesList.add(uniqueVertices.get(newVertex));
            }
        }

        // Convert lists to arrays if needed
        this.indices = indicesList.stream().mapToInt(i -> i).toArray();
    }
}
