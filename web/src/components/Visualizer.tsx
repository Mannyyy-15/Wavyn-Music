import React, { useEffect, useRef } from 'react';
import { AudioEngine } from '../services/audioEngine';
import { VisualizerMode } from '../types';

interface VisualizerProps {
  mode: VisualizerMode;
  accentColor?: string;
  className?: string;
}

export const Visualizer: React.FC<VisualizerProps> = ({ mode, accentColor = '#3b82f6', className = '' }) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    if (mode === 'off') return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    const engine = AudioEngine.getInstance();

    const render = () => {
      animationFrameId = requestAnimationFrame(render);
      const width = canvas.width;
      const height = canvas.height;
      ctx.clearRect(0, 0, width, height);

      const freqData = engine.getFrequencyData();
      const numBars = 32;
      const barWidth = (width / numBars) * 0.7;
      const gap = (width / numBars) * 0.3;

      if (mode === 'bars') {
        for (let i = 0; i < numBars; i++) {
          const value = freqData[i] || 0;
          const percent = value / 255;
          const barHeight = Math.max(4, percent * height * 0.9);

          const gradient = ctx.createLinearGradient(0, height, 0, height - barHeight);
          gradient.addColorStop(0, accentColor);
          gradient.addColorStop(1, '#60a5fa');

          ctx.fillStyle = gradient;
          ctx.beginPath();
          ctx.roundRect(i * (barWidth + gap), height - barHeight, barWidth, barHeight, [4, 4, 0, 0]);
          ctx.fill();
        }
      } else if (mode === 'wave') {
        ctx.beginPath();
        ctx.lineWidth = 3;
        ctx.strokeStyle = accentColor;
        ctx.shadowBlur = 10;
        ctx.shadowColor = accentColor;

        const sliceWidth = width / numBars;
        let x = 0;

        for (let i = 0; i < numBars; i++) {
          const v = (freqData[i] || 128) / 128.0;
          const y = (v * height) / 2;

          if (i === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
          x += sliceWidth;
        }

        ctx.lineTo(width, height / 2);
        ctx.stroke();
        ctx.shadowBlur = 0;
      }
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, [mode, accentColor]);

  if (mode === 'off') return null;

  return (
    <canvas
      ref={canvasRef}
      width={240}
      height={60}
      className={`rounded-lg ${className}`}
      style={{ filter: 'drop-shadow(0 0 8px rgba(59, 130, 246, 0.4))' }}
    />
  );
};
