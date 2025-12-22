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

uniform float u_vhsEnabled;
uniform float u_vhsIntensity;

float random(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n = i.x + i.y * 57.0;
    return mix(mix(random(vec2(n, n)), random(vec2(n + 1.0, n)), f.x),
               mix(random(vec2(n, n + 57.0)), random(vec2(n + 1.0, n + 57.0)), f.x), f.y);
}

float vhsTrackingLines(vec2 uv, float time) {
    float line1 = sin((uv.y - time * 0.1) * 50.0) * 0.5 + 0.5;
    float line2 = sin((uv.y + time * 0.07) * 80.0) * 0.5 + 0.5;
    float line3 = sin((uv.y - time * 0.13) * 120.0) * 0.5 + 0.5;
    
    float intensity = line1 * 0.2 + line2 * 0.15 + line3 * 0.1;
    
    if (random(vec2(floor(time * 2.0), floor(uv.y * 10.0))) > 0.96) {
        intensity += 0.35;
    }
    
    return intensity;
}

vec2 vhsTapeWobble(vec2 uv, float time) {
    float wobble1 = sin(uv.y * 10.0 + time * 2.0) * 0.006;
    float wobble2 = sin(uv.y * 25.0 - time * 3.0) * 0.004;
    float wobble3 = noise(vec2(uv.y * 5.0, time * 0.5)) * 0.01;
    
    float glitchWobble = 0.0;
    if (random(vec2(time * 3.0, uv.y)) > 0.975) {
        glitchWobble = (random(vec2(time, uv.y)) - 0.5) * 0.025;
    }
    
    uv.x += wobble1 + wobble2 + wobble3 + glitchWobble;
    return uv;
}

vec4 vhsChromaBleeding(sampler2D tex, vec2 uv, float time) {
    vec2 offset1 = vec2(0.002, 0.0) * sin(time * 5.0);
    vec2 offset2 = vec2(0.003, 0.0) * cos(time * 3.0);
    
    float r = texture2D(tex, uv - offset1).r;
    float g = texture2D(tex, uv + offset2 * 1.2).g;
    float b = texture2D(tex, uv + offset1 * 0.5).b;
    
    g += 0.03;
    
    return vec4(r, g, b, 1.0);
}

vec3 vhsNoise(vec3 color, vec2 uv, float time) {
    float noise1 = random(uv + time * 10.0) * 0.08;
    float noise2 = noise(uv * 100.0 + time * 50.0) * 0.05;
    
    if (random(vec2(time * 5.0, uv.y * 20.0)) > 0.98) {
        noise1 += random(uv * time) * 0.25;
    }
    
    color += vec3(noise1 + noise2);
    
    return color;
}

vec4 vhsRGBShift(sampler2D tex, vec2 uv, float time) {
    float shift = sin(time * 10.0 + uv.y * 50.0) * 0.005;
    
    if (random(vec2(floor(time * 10.0), floor(uv.y * 20.0))) > 0.97) {
        shift += (random(vec2(time, uv.y)) - 0.5) * 0.025;
    }
    
    float r = texture2D(tex, uv + vec2(shift, 0.0)).r;
    float g = texture2D(tex, uv).g;
    float b = texture2D(tex, uv - vec2(shift, 0.0)).b;
    
    return vec4(r, g, b, 1.0);
}

float vhsScanlineJitter(vec2 uv, float time) {
    float jitter = sin(uv.y * u_resolution.y * 0.5 + time * 50.0) * 0.004;
    
    if (random(vec2(floor(time * 20.0), floor(uv.y * 100.0))) > 0.96) {
        jitter += (random(vec2(time * 10.0, uv.y)) - 0.5) * 0.015;
    }
    
    return jitter;
}

float vhsDropouts(vec2 uv, float time) {
    float dropout = 0.0;
    
    vec2 blockPos = floor(uv * vec2(20.0, 15.0) + vec2(time * 2.0, 0.0));
    if (random(blockPos) > 0.98) {
        dropout = random(blockPos + time) * 0.5;
    }
    
    if (random(vec2(uv.y * 100.0, floor(time * 5.0))) > 0.985) {
        dropout += 0.4;
    }
    
    return dropout;
}

float vhsHeadSwitching(vec2 uv, float time) {
    float switchingBand = smoothstep(0.05, 0.08, uv.y) * (1.0 - smoothstep(0.08, 0.13, uv.y));
    float noise = random(vec2(uv.x * 50.0, time * 10.0)) * 0.6;
    return switchingBand * noise;
}

vec3 applyLainGreenHorror(vec3 color, float time) {
    color.r *= 0.93;
    color.g *= 1.08;
    color.b *= 0.90;
    
    color.g += 0.04;
    
    if (random(vec2(time * 2.0, color.g)) > 0.992) {
        color.g += 0.15;
    }
    
    return color;
}

// ===== CRT FUNCTIONS (старые) =====

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
        return 1.0 - u_maskIntensity;
    } else if (modX < 2.0) {
        return 1.0;
    } else {
        return 1.0 - u_maskIntensity * 0.5;
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

// ===== MAIN =====

void main() {
    vec2 uv = v_texCoords;
    
    if (u_vhsEnabled > 0.5) {
        
        vec4 color = texture2D(u_texture, uv);
        
        color.rgb = applyLainGreenHorror(color.rgb, u_time);
        
        uv = vhsTapeWobble(uv, u_time);
        uv.x += vhsScanlineJitter(uv, u_time);
        
        color = vhsRGBShift(u_texture, uv, u_time);
        color = mix(color, vhsChromaBleeding(u_texture, uv, u_time), 0.5);
        color.rgb = vhsNoise(color.rgb, uv, u_time);
        
        float trackingLines = vhsTrackingLines(uv, u_time);
        color.rgb += vec3(0.0, trackingLines * 0.4, trackingLines * 0.3);
        
        float dropouts = vhsDropouts(uv, u_time);
        color.rgb *= (1.0 - dropouts);
        
        float headSwitching = vhsHeadSwitching(uv, u_time);
        color.rgb *= (1.0 - headSwitching);
        
        color.rgb = applyLainGreenHorror(color.rgb, u_time);
        
        color.rgb = mix(texture2D(u_texture, v_texCoords).rgb, color.rgb, u_vhsIntensity);
        
        gl_FragColor = color * v_color;
        return;
    }
    
    uv = applyCurvature(uv);
    
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
