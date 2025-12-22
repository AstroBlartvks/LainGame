#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float u_time;

uniform float u_scanlineIntensity;
uniform float u_vignetteIntensity;
uniform float u_bloomIntensity;
uniform vec2 u_curvature;
uniform float u_aberration;
uniform float u_flickerIntensity;
uniform float u_maskIntensity;

uniform float u_colorEnabled;
uniform float u_brightness;
uniform float u_contrast;
uniform float u_saturation;
uniform vec3 u_warmth;

vec2 applyCurvature(vec2 uv) {
    vec2 cc = uv - 0.5;
    float dist = dot(cc, cc) * 0.2;
    return uv + cc * (u_curvature / 10.0) * dist;
}

bool isOutOfBounds(vec2 uv) {
    return uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0;
}

vec4 applyAberration(vec2 uv) {
    vec2 offset = (uv - 0.5) * u_aberration;
    float r = texture2D(u_texture, uv + offset).r;
    float g = texture2D(u_texture, uv).g;
    float b = texture2D(u_texture, uv - offset).b;
    float a = texture2D(u_texture, uv).a;
    return vec4(r, g, b, a);
}

float applyScanlines(vec2 uv) {
    float scanline = sin(uv.y * u_resolution.y * 3.14159) * 0.5 + 0.5;
    return 1.0 - (scanline * u_scanlineIntensity);
}

float applyVignette(vec2 uv) {
    vec2 position = uv - 0.5;
    float dist = length(position);
    float vignette = smoothstep(0.75, 0.45, dist);
    return vignette * (1.0 - u_vignetteIntensity) + u_vignetteIntensity;
}

vec3 applyBloom(vec3 color) {
    float brightness = dot(color, vec3(0.299, 0.587, 0.114));
    if (brightness > 0.7) {
        vec3 bloom = color * (brightness - 0.7) * u_bloomIntensity;
        return color + bloom;
    }
    return color;
}

float applyFlicker() {
    float flicker = sin(u_time * 60.0) * 0.5 + 0.5;
    return flicker * u_flickerIntensity + (1.0 - u_flickerIntensity);
}

float applyShadowMask(vec2 uv) {
    vec2 pixelPos = uv * u_resolution;
    float modX = mod(pixelPos.x, 3.0);

    if (modX < 1.0) {
        return 1.0 - u_maskIntensity; // R
    } else if (modX < 2.0) {
        return 1.0; // G
    } else {
        return 1.0 - u_maskIntensity * 0.5; // B
    }
}

vec3 applyColorCorrection(vec3 color) {
    if (u_colorEnabled < 0.5) {
        return color;
    }

    color = ((color - 0.5) * u_contrast + 0.5) * u_brightness;

    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(vec3(gray), color, u_saturation);

    color *= u_warmth;

    return color;
}

void main() {
    vec2 uv = applyCurvature(v_texCoords);

    // Check bounds
    if (isOutOfBounds(uv)) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec4 color = applyAberration(uv);

    color.rgb *= applyScanlines(uv);

    color.rgb *= applyVignette(uv);

    color.rgb = applyBloom(color.rgb);

    color.rgb *= applyFlicker();

    color.rgb *= applyShadowMask(uv);

    color.rgb = applyColorCorrection(color.rgb);

    gl_FragColor = color * v_color;
}
