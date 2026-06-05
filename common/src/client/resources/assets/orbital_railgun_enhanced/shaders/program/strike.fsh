#version 150 compatibility
#define STEPS 1000
#define MIN_DIST 0.001
#define MAX_DIST 5000.

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseTransformMatrix;
uniform mat4 ModelViewMat;
uniform vec3 CameraPosition;
uniform vec3 BlockPosition;

uniform float iTime;

const vec3 blue = vec3(0.2, 0.5, 1.0);
const vec3 cyan = vec3(0.0, 1.0, 1.0);
const vec3 white = vec3(1.0, 1.0, 1.0);
const vec3 yellow = vec3(1.0, 0.95, 0.0);
const vec3 orange = vec3(1.0, 0.4, 0.0);
const vec3 red = vec3(1.0, 0.0, 0.0);
const vec3 purple = vec3(0.9, 0.0, 1.0);
const vec3 magenta = vec3(1.0, 0.0, 0.6);

const float startTime = 4.;
const float expansionTime = 32.;
const float endTime = startTime + expansionTime;
float localTime = 0.;

in vec2 texCoord;
out vec4 fragColor;

float smooth_min(float a, float b, float k) {
    float diff = a - b;
    return 0.5 * (a + b - sqrt(diff * diff + k * k * k));
}

vec2 rotate(vec2 p, float r) {
    return mat2(cos(r), -sin(r), sin(r), cos(r)) * p;
}

float sDist(vec3 p) {
    if (iTime < endTime) {
        float scale = 1.2 * (expansionTime * 25. / (localTime + 0.7) / 32. - 32. * (1. - pow(clamp(2. * localTime / expansionTime - 1., 0., 10.), 2.))) + 2.4;
        float main_sphere = abs(length(p) + scale);

        float rotation = min(25. / (localTime - 25.) + 1.5, 0);
        p.xz = rotate(p.xz, rotation);

        // 32 beams
        const float num = 6.28 / 32.;
        float offset = -pow((localTime - 13.) / 1.4, 2.) + 150.;
        float theta = atan(p.z, p.x);
        theta = floor(theta / num);

        float c1 = num * (theta + 0.0);
        vec3 p1 = mat3(cos(c1), 0., -sin(c1), 0., 1., 0., sin(c1), 0., cos(c1)) * p;
        float c2 = num * (theta + 1.0);
        vec3 p2 = mat3(cos(c2), 0., -sin(c2), 0., 1., 0., sin(c2), 0., cos(c2)) * p;

        p1.x -= offset;
        p2.x -= offset;

        float outer_spheres = min(length(p1) + max(scale, -3), length(p2) + max(scale, -3));

        float base = clamp(pow(localTime - 16., 3.) * 50., 0., 5000.);
        float height = min(pow(localTime - 11., 3.) * 50., 5000.);
        // 1.0 radius beams
        float outer_beams = min(
            length(vec3(p1.x, clamp(p1.y, base, height) - p1.y, p1.z)) - 1.0,
            length(vec3(p2.x, clamp(p2.y, base, height) - p2.y, p2.z)) - 1.0
        );

        return smooth_min(main_sphere, smooth_min(outer_spheres, outer_beams, 1.), 5.);
    }

    // explosion visual radius 100
    float explosion_cylindar = length(p.xz) + 8. / (localTime) - 100.;
    return explosion_cylindar;
}

vec2 raycast(vec3 point, vec3 dir) {
    float traveled = 0.;
    int close_steps = 0;
    for (int i = 0; i < STEPS; i++) {
        float safe = sDist(point);
        if (safe <= MIN_DIST || traveled >= MAX_DIST) break;
        traveled += safe;
        point += dir * safe;
        if (safe <= 0.01) close_steps += 1;
    }
    return vec2(traveled, close_steps);
}

vec3 worldPos(vec3 point) {
    vec3 ndc = point * 2.0 - 1.0;
    vec4 homPos = InverseTransformMatrix * vec4(ndc, 1.0);
    vec3 viewPos = homPos.xyz / homPos.w;
    return (inverse(ModelViewMat) * vec4(viewPos, 1.)).xyz + CameraPosition;
}

float shockwave(vec3 point) {
    float dist = sDist(point);
    float default_light = 40. / pow(dist, 2.);

    if (iTime < endTime) {
        float speed_factor = 1. - pow(localTime / expansionTime, 2.);
        speed_factor = clamp(5., -5., speed_factor);
        // 10 shockwave rings, moderate brightness
        float shock = 0.04 / abs(fract(10. * dist / expansionTime - localTime * speed_factor) - 0.5) * 1.5;
        return default_light + shock * smoothstep(dist + 4., dist + 14., localTime * speed_factor * expansionTime / 2.);
    }

    float fade_factor = clamp(5. / localTime - 0.25, 0., 1.);
    return fade_factor * (default_light
        + 80. / abs(dist - 120. * (localTime - 0.5)) - 0.3
        + 30. / abs(dist - 60. * (localTime - 0.5)) - 0.2
    ) + smoothstep(dist - 10., dist, 200. * (localTime - 2.5));
}

float sdBox(vec2 p, vec2 s) {
    p = abs(p) - s;
    return length(max(p, 0.)) + min(max(p.x, p.y), 0.);
}

void main() {
    vec3 original = texture(DiffuseSampler, texCoord).rgb;
    localTime = iTime - startTime * step(startTime, iTime) - expansionTime * step(endTime, iTime);

    float depth = texture(DepthSampler, texCoord).r;
    vec3 start_point = worldPos(vec3(texCoord, 0)) - BlockPosition;
    vec3 end_point = worldPos(vec3(texCoord, depth)) - BlockPosition;
    vec3 dir = normalize(end_point - start_point);

    if (iTime < startTime) {
        end_point.xz = rotate(end_point.xz, pow(localTime / 2., 4.));
        float dist = max(max(length(end_point.xz) - 24., -(length(end_point.xz) - 12. * (localTime - 1.7))), -min(sdBox(end_point.xz, vec2(0., 24.)), sdBox(end_point.xz, vec2(24., 0.))));
        vec3 col = original + 0.5 / pow(dist, 2.) * magenta * step(length(end_point), localTime * 20.);
        col = mix(col, vec3(0.), pow(max(localTime - 3., 0.), 2.)), 1.;
        fragColor = vec4(col, 1.);
        return;
    }

    vec2 hit_result = raycast(start_point, dir);
    vec3 hit_point = start_point + dir * hit_result.x;

    // smooth color shift — impressive but safe, no rapid flashing
    float t = localTime / expansionTime;
    vec3 strikeColor;
    // one slow sweep: purple -> blue -> cyan -> white -> orange -> red
    // each transition takes ~16% of the strike duration
    if (t < 0.2) strikeColor = mix(purple, blue, t / 0.2);
    else if (t < 0.4) strikeColor = mix(blue, cyan, (t - 0.2) / 0.2);
    else if (t < 0.6) strikeColor = mix(cyan, white, (t - 0.4) / 0.2);
    else if (t < 0.8) strikeColor = mix(white, orange, (t - 0.6) / 0.2);
    else strikeColor = mix(orange, red, (t - 0.8) / 0.2);

    vec3 col = mix(strikeColor, vec3(0.), abs(sin(3.14 * localTime / expansionTime)))
        + vec3(smoothstep(2., 6., hit_result.y)) * strikeColor * 2.0;

    float threshold = step(sDist(hit_point), MIN_DIST * 2.);
    threshold *= step(distance(start_point, hit_point), distance(start_point, end_point));
    threshold *= 1. - pow(clamp(iTime / endTime - 1., 0., 1.), 2.);

    vec3 shockwave_color = mix(strikeColor, white, clamp(iTime / endTime - 1., 0., 1.));

    fragColor = vec4(mix(original * shockwave(end_point) * shockwave_color, col, threshold), 1.);
}
