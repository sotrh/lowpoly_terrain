package com.sotrh.lowpoly_terrain.common

val iNULL = 0
val lNULL = 0L

val DEFAULT_VERTEX_SHADER =
        """
            #version 150 core

            in vec3 position;
            in vec3 color;

            out vec3 vertexColor;

            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;

            void main() {
                vertexColor = color;
                mat4 mvp = projection * view * model;
                gl_Position = mvp * vec4(position, 1.0);
            }
        """

val DEFAULT_FRAGMENT_SHADER =
        """
            #version 150 core

            in vec3 vertexColor;

            out vec4 fragColor;

            void main() {
                fragColor = vec4(vertexColor, 1.0);
            }
        """

val MATRIX_BUFFER = FloatArray(16)