import React from 'react';

export const AnimatedMeshBackground: React.FC = () => {
  return (
    <div className="fixed inset-0 pointer-events-none -z-10 overflow-hidden bg-[#000000]">
      {/* 1. Base Gradient from top to bottom */}
      <div
        className="absolute inset-0"
        style={{
          background: 'linear-gradient(180deg, #0f0503 0%, #0a0302 25%, #050202 60%, #000000 100%)',
        }}
      />

      {/* 2. Primary Top-to-Bottom Glowing Header Flare */}
      <div
        className="absolute -top-32 left-1/2 -translate-x-1/2 w-[110vw] h-[550px] animate-mesh-pulse"
        style={{
          background:
            'radial-gradient(ellipse 70% 60% at 50% 15%, rgba(241, 96, 1, 0.26) 0%, rgba(193, 8, 1, 0.18) 45%, rgba(0, 0, 0, 0) 80%)',
          filter: 'blur(30px)',
        }}
      />

      {/* 3. Floating Orb 1: Vibrant Flame Orange (#F16001 / #E85002) */}
      <div
        className="absolute -top-20 -left-20 w-[550px] h-[550px] rounded-full animate-mesh-orb-1"
        style={{
          background: 'radial-gradient(circle, rgba(232, 80, 2, 0.22) 0%, rgba(241, 96, 1, 0.12) 50%, transparent 75%)',
          filter: 'blur(90px)',
        }}
      />

      {/* 4. Floating Orb 2: Deep Crimson Ember (#C10801) */}
      <div
        className="absolute top-10 -right-20 w-[600px] h-[500px] rounded-full animate-mesh-orb-2"
        style={{
          background: 'radial-gradient(circle, rgba(193, 8, 1, 0.22) 0%, rgba(193, 8, 1, 0.08) 55%, transparent 80%)',
          filter: 'blur(100px)',
        }}
      />

      {/* 5. Floating Orb 3: Warm Champagne Accent (#D9C3AB) */}
      <div
        className="absolute top-[28%] left-[25%] w-[420px] h-[340px] rounded-full animate-mesh-orb-3"
        style={{
          background: 'radial-gradient(circle, rgba(217, 195, 171, 0.12) 0%, rgba(241, 96, 1, 0.06) 50%, transparent 75%)',
          filter: 'blur(80px)',
        }}
      />

      {/* 6. Subtle Vignette for depth */}
      <div
        className="absolute inset-0"
        style={{
          background: 'radial-gradient(circle at 50% 30%, transparent 40%, rgba(0, 0, 0, 0.65) 90%)',
        }}
      />
    </div>
  );
};
export default AnimatedMeshBackground;
