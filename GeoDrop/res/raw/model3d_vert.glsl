attribute vec3 in_vertex;
attribute vec2 in_uv;

uniform mat4 u_model;
uniform mat4 u_view;
uniform mat4 u_proj;

varying vec2 v_uv;

void main(void) {
  v_uv = in_uv;

  gl_Position = u_proj * u_view * u_model * vec4(in_vertex, 1.0f);
}
