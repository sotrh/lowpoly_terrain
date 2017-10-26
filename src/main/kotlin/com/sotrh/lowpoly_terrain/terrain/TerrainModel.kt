package com.sotrh.lowpoly_terrain.terrain

import com.sotrh.lowpoly_terrain.common.*
import org.joml.Vector3f

class TerrainModel(private val terrain: Terrain) : Model(
        Shader(VERTEX_SHADER, FRAGMENT_SHADER, GEOMETRY_SHADER),
        VAO.Builder()
                .triangles()
                .vertices(terrain.size * terrain.size, FLOATS_PER_VERTEX, true) { buffer, vertexCount ->
                    (0 until terrain.size).forEach { x ->
                        (0 until terrain.size).forEach { z ->
                            val height = terrain.heightMap[x][z]

                            // position
                            buffer.put(x - terrain.size * 0.5f - 0.5f).put(height).put(z - terrain.size * 0.5f - 0.5f)

                            // color
                            val colorLerp = Math.max(Math.min((VERTEX_COLORS.size - 1f) * height / 10f, VERTEX_COLORS.size - 1f), 0f)
                            val colorIndex = colorLerp.toInt()
                            val vertexColor = Vector3f()

                            if (colorIndex <= 0) vertexColor.set(VERTEX_COLORS[0])
                            else if (colorIndex >= VERTEX_COLORS.size - 1) vertexColor.set(VERTEX_COLORS[VERTEX_COLORS.size - 1])
                            else {
                                val a = VERTEX_COLORS[colorIndex]
                                val b = VERTEX_COLORS[colorIndex + 1]
                                val lerp = colorLerp - colorIndex
                                vertexColor.set(
                                        a.x * (1 - lerp) + b.x * lerp,
                                        a.y * (1 - lerp) + b.y * lerp,
                                        a.z * (1 - lerp) + b.z * lerp
                                )
                            }

                            buffer.put(vertexColor.x).put(vertexColor.y).put(vertexColor.z)
                        }
                    }
                    buffer.flip()
                }

                // element count for triangles:
                // 2 triangles per square
                // 3 elements per triangle
                // 6 elements per square
                // (terrain.size - 1) ^ 2 squares
                // total elements = 6 * (terrain.size - 1) ^ 2
                .elements(6 * (terrain.size - 1) * (terrain.size - 1), true) { buffer, _ ->
                    fun elementFor(x: Int, z: Int): Int {
                        return (x * terrain.size + z)
                    }

                    // since triangles are per square, has to skip the last vertices of the mesh
                    var index = 0
                    (0..terrain.size - 2).forEach { x ->
                        (0..terrain.size - 2).forEach { z ->
                            if (index % 2 == 0) {
                                // 1st triangle
                                buffer.put(elementFor(x, z + 1))
                                buffer.put(elementFor(x, z))
                                buffer.put(elementFor(x + 1, z + 1))

                                // 2nd triangle
                                buffer.put(elementFor(x + 1, z))
                                buffer.put(elementFor(x + 1, z + 1))
                                buffer.put(elementFor(x, z))
                            } else {
                                // 1st triangle
                                buffer.put(elementFor(x, z))
                                buffer.put(elementFor(x + 1, z))
                                buffer.put(elementFor(x, z + 1))

                                // 2nd triangle
                                buffer.put(elementFor(x + 1, z + 1))
                                buffer.put(elementFor(x, z + 1))
                                buffer.put(elementFor(x + 1, z))
                            }

                            index++
                        }
                    }
                    buffer.flip()
                }
                .build()
) {

    companion object {
        val FLOATS_PER_VERTEX = 6

        // bottom to top
        val VERTEX_COLORS = arrayOf(Vector3f(0f, 0f, 1f), Vector3f(0.5f, 0.5f, 0f), Vector3f(0f, 1f, 0f), Vector3f(1f, 1f, 1f))

        val VERTEX_SHADER = """
            #version 150

            in vec3 position;
            in vec3 color;

            out vec3 vertexColor;

            void main() {
                vertexColor = color;
                gl_Position = vec4(position, 1.0);
            }
        """

        val GEOMETRY_SHADER = """
            #version 330

            layout(triangles) in;
            layout(triangle_strip, max_vertices = 3) out;

            in vec3 vertexColor[];

            out vec3 faceColor;

            uniform vec3 lightDirection;
            uniform vec3 lightColor;
            uniform vec2 lightBias;

            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;

            vec3 calculateLighting(vec3 normal) {
                float brightness = max(dot(-lightDirection, normal), 0.0);
                return (lightColor * lightBias.x) + (brightness * lightColor * lightBias.y);
            }

            vec3 calcTriangleNormal() {
                vec3 tangent1 = gl_in[1].gl_Position.xyz - gl_in[0].gl_Position.xyz;
                vec3 tangent2 = gl_in[2].gl_Position.xyz - gl_in[0].gl_Position.xyz;
                vec3 normal = cross(tangent1, tangent2);
                return normalize(normal);
            }

            void main(void) {
                vec3 normal = calcTriangleNormal();
                vec3 lighting = calculateLighting(normal);

                for (int i=0; i<3; i++) {
                    gl_Position = projection * view * model * gl_in[i].gl_Position;
                    faceColor = vertexColor[0] * lighting;
                    EmitVertex();
                }

                EndPrimitive();
            }
        """

        val FRAGMENT_SHADER = """
            #version 150 core

            in vec3 faceColor;

            out vec4 fragColor;

            void main() {
                fragColor = vec4(faceColor, 1.0);
            }
        """
    }

    fun destroy() {
        vao.delete()
    }

}