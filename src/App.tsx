/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Radio, Terminal, Delete, Share2, Info, ChevronRight, Hash } from 'lucide-react';

// --- Types ---
type MorseNode = {
  id: string;
  char: string;
  dot?: MorseNode;
  dash?: MorseNode;
};

// --- Morse Tree Construction ---
// Note: Right is Dot (.), Left is Dash (-) based on the image logic
const MORSE_TREE: MorseNode = {
  id: 'root',
  char: '',
  // RIGHT (DOT)
  dot: {
    id: 'e', char: 'E',
    dot: {
      id: 'i', char: 'I',
      dot: {
        id: 's', char: 'S',
        dot: { id: 'h', char: 'H' },
        dash: { id: 'v', char: 'V' }
      },
      dash: {
        id: 'u', char: 'U',
        dot: { id: 'f', char: 'F' }
      }
    },
    dash: {
      id: 'a', char: 'A',
      dot: {
        id: 'r', char: 'R',
        dot: { id: 'l', char: 'L' }
      },
      dash: {
        id: 'w', char: 'W',
        dot: { id: 'p', char: 'P' },
        dash: { id: 'j', char: 'J' }
      }
    }
  },
  // LEFT (DASH)
  dash: {
    id: 't', char: 'T',
    dot: {
      id: 'n', char: 'N',
      dot: {
        id: 'd', char: 'D',
        dot: { id: 'b', char: 'B' },
        dash: { id: 'x', char: 'X' }
      },
      dash: {
        id: 'k', char: 'K',
        dot: { id: 'c', char: 'C' },
        dash: { id: 'y', char: 'Y' }
      }
    },
    dash: {
      id: 'm', char: 'M',
      dot: {
        id: 'g', char: 'G',
        dot: { id: 'z', char: 'Z' },
        dash: { id: 'q', char: 'Q' }
      },
      dash: { id: 'o', char: 'O' }
    }
  }
};

const DOT_THRESHOLD = 200; // ms
const AUTO_SUBMIT_DELAY = 1000; // ms

export default function App() {
  const [currentNode, setCurrentNode] = useState<MorseNode | null>(MORSE_TREE);
  const [history, setHistory] = useState<MorseNode[]>([]);
  const [message, setMessage] = useState<string>('');
  const [isPressing, setIsPressing] = useState(false);
  const [pressStart, setPressStart] = useState<number | null>(null);
  const [lastInputTime, setLastInputTime] = useState<number>(0);
  const [currentSequence, setCurrentSequence] = useState<string>('');
  const [showSettings, setShowSettings] = useState(false);
  
  // Settings state
  const [settings, setSettings] = useState({
    sound: true,
    vibration: true,
    frequency: 700,
  });

  const audioContext = useRef<AudioContext | null>(null);
  const oscillator = useRef<OscillatorNode | null>(null);
  const gainNode = useRef<GainNode | null>(null);

  const initAudio = () => {
    if (!audioContext.current) {
      audioContext.current = new (window.AudioContext || (window as any).webkitAudioContext)();
      gainNode.current = audioContext.current.createGain();
      gainNode.current.connect(audioContext.current.destination);
      gainNode.current.gain.value = 0;
    }
  };

  const startTone = () => {
    if (!settings.sound) return;
    initAudio();
    if (audioContext.current && gainNode.current) {
      oscillator.current = audioContext.current.createOscillator();
      oscillator.current.type = 'sine';
      oscillator.current.frequency.setValueAtTime(settings.frequency, audioContext.current.currentTime);
      oscillator.current.connect(gainNode.current);
      oscillator.current.start();
      gainNode.current.gain.setTargetAtTime(0.1, audioContext.current.currentTime, 0.01);
    }
  };

  const stopTone = () => {
    if (gainNode.current && audioContext.current) {
      gainNode.current.gain.setTargetAtTime(0, audioContext.current.currentTime, 0.01);
      setTimeout(() => {
        oscillator.current?.stop();
        oscillator.current?.disconnect();
      }, 50);
    }
  };

  const triggerVibration = (ms: number) => {
    if (settings.vibration && navigator.vibrate) {
      navigator.vibrate(ms);
    }
  };

  const resetToRoot = useCallback(() => {
    setCurrentNode(MORSE_TREE);
    setCurrentSequence('');
  }, []);

  const handleInput = useCallback((type: 'dot' | 'dash') => {
    if (!currentNode) return;

    const nextNode = type === 'dot' ? currentNode.dot : currentNode.dash;
    if (nextNode) {
      setCurrentNode(nextNode);
      setCurrentSequence(prev => prev + (type === 'dot' ? '·' : '−'));
      setLastInputTime(Date.now());
    } else {
      // If we go out of bounds, maybe error or just stick
      // For this app, let's flash a warning or just do nothing
    }
  }, [currentNode]);

  // handle auto-submit
  useEffect(() => {
    if (currentNode && currentNode !== MORSE_TREE) {
      const timer = setTimeout(() => {
        setMessage(prev => prev + currentNode.char);
        resetToRoot();
      }, AUTO_SUBMIT_DELAY);
      return () => clearTimeout(timer);
    }
  }, [currentNode, resetToRoot]);

  const handlePressStart = (e: React.MouseEvent | React.TouchEvent) => {
    e.preventDefault();
    setIsPressing(true);
    setPressStart(Date.now());
    startTone();
    triggerVibration(20);
  };

  const handlePressEnd = () => {
    if (!pressStart) return;
    const duration = Date.now() - pressStart;
    setIsPressing(false);
    setPressStart(null);
    stopTone();

    if (duration < DOT_THRESHOLD) {
      handleInput('dot');
    } else {
      handleInput('dash');
    }
  };

  const clearMessage = () => setMessage('');
  const backspaceMessage = () => setMessage(prev => prev.slice(0, -1));

  return (
    <div className="min-h-screen bg-[#0A0A0B] text-[#D1D5DB] font-sans flex flex-col relative border-8 border-[#1A1A1C] overflow-hidden">
      {/* Decorative Circuit Background */}
      <div className="absolute inset-0 pointer-events-none opacity-20" 
           style={{ backgroundImage: 'radial-gradient(#2DD4BF 0.5px, transparent 0.5px)', backgroundSize: '24px 24px' }}>
      </div>

      {/* Header */}
      <header className="h-20 border-b border-[#2DD4BF]/20 flex items-center justify-between px-6 md:px-10 bg-[#0F172A]/80 backdrop-blur-md z-10">
        <div className="flex items-center gap-4">
          <div className="w-3 h-3 rounded-full bg-[#2DD4BF] shadow-[0_0_10px_#2DD4BF]"></div>
          <h1 className="text-lg md:text-xl font-bold tracking-widest text-white uppercase">
            Signal Processor <span className="text-[#2DD4BF] font-mono opacity-60">v.2.4</span>
          </h1>
        </div>
        
        <div className="flex items-center gap-3">
           <button 
             onClick={() => setShowSettings(true)}
             className="p-2 hover:bg-white/5 rounded-full transition-colors"
           >
              <Info className="w-4 h-4 text-[#2DD4BF]" />
           </button>
        </div>
      </header>

      {/* Settings Modal */}
      <AnimatePresence>
        {showSettings && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4"
          >
            <motion.div 
              initial={{ scale: 0.9, y: 20 }}
              animate={{ scale: 1, y: 0 }}
              className="w-full max-w-md bg-[#111827] border border-[#2DD4BF]/30 rounded-3xl p-8 shadow-2xl"
            >
              <div className="flex justify-between items-center mb-8">
                <h2 className="text-xl font-bold text-white tracking-widest uppercase">Configuration</h2>
                <button onClick={() => setShowSettings(false)} className="text-slate-400 hover:text-white transition-colors">
                  <Hash className="w-5 h-5 rotate-45" />
                </button>
              </div>

              <div className="space-y-6">
                <div className="flex items-center justify-between p-4 bg-[#0A0A0B] rounded-2xl border border-white/5">
                  <div className="flex items-center gap-3">
                    <Radio className="w-4 h-4 text-[#2DD4BF]" />
                    <span className="text-sm font-medium">Buzzer (Audio)</span>
                  </div>
                  <button 
                    onClick={() => setSettings(s => ({ ...s, sound: !s.sound }))}
                    className={`w-12 h-6 rounded-full transition-colors relative ${settings.sound ? 'bg-[#2DD4BF]' : 'bg-slate-700'}`}
                  >
                    <motion.div 
                      animate={{ x: settings.sound ? 24 : 4 }}
                      className="absolute top-1 w-4 h-4 bg-white rounded-full"
                    />
                  </button>
                </div>

                <div className="flex items-center justify-between p-4 bg-[#0A0A0B] rounded-2xl border border-white/5">
                  <div className="flex items-center gap-3">
                    <Share2 className="w-4 h-4 text-[#2DD4BF]" />
                    <span className="text-sm font-medium">Haptic Feedback</span>
                  </div>
                  <button 
                    onClick={() => setSettings(s => ({ ...s, vibration: !s.vibration }))}
                    className={`w-12 h-6 rounded-full transition-colors relative ${settings.vibration ? 'bg-[#2DD4BF]' : 'bg-slate-700'}`}
                  >
                    <motion.div 
                      animate={{ x: settings.vibration ? 24 : 4 }}
                      className="absolute top-1 w-4 h-4 bg-white rounded-full"
                    />
                  </button>
                </div>
              </div>

              <button 
                onClick={() => setShowSettings(false)}
                className="w-full mt-8 py-4 bg-gradient-to-r from-[#2DD4BF] to-teal-600 text-[#0A0A0B] rounded-2xl font-black uppercase tracking-widest hover:opacity-90 transition-opacity"
              >
                Apply System Sync
              </button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Content */}
      <main className="flex-1 flex flex-col lg:flex-row p-4 md:p-8 gap-6 md:gap-10 z-10 overflow-hidden">
        
        {/* Left Section: Tree Visualization */}
        <section className="flex-[1.2] flex flex-col gap-6">
          <div className="flex-1 bg-[#111827] rounded-2xl border border-[#2DD4BF]/10 p-6 flex flex-col items-center justify-center relative overflow-hidden shadow-2xl">
            <div className="absolute top-4 left-6 font-mono text-[10px] text-[#2DD4BF]/40 uppercase tracking-widest">
              Dichotomous Logic Path // TREE_ROOT_α
            </div>
            
            <svg className="w-full flex-1 min-h-[400px]" viewBox="-250 0 500 500" preserveAspectRatio="xMidYMin meet">
              <defs>
                <filter id="glow-teal">
                  <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                  <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                  </feMerge>
                </filter>
              </defs>
              {
                // Updated renderNode logic with theme colors
                (function renderThemedNode(node: MorseNode, x: number, y: number, level: number, pX: number, pY: number) {
                  const isActive = currentNode?.id === node.id;
                  const isRoot = node.id === 'root';
                  const yStep = 60;
                  const xStep = Math.max(160 / (level + 1), 30);

                  return (
                    <React.Fragment key={node.id}>
                      {!isRoot && (
                        <line
                          x1={pX} y1={pY} x2={x} y2={y}
                          stroke={isActive ? '#2DD4BF' : '#1F2937'}
                          strokeWidth={isActive ? "3" : "1.5"}
                          className="transition-all duration-300"
                        />
                      )}
                      <g transform={`translate(${x}, ${y})`}>
                        <motion.circle
                          r={isRoot ? 12 : 10}
                          fill={isActive ? '#2DD4BF' : '#0F172A'}
                          stroke={isActive ? '#2DD4BF' : '#374151'}
                          strokeWidth="2"
                          animate={{
                            scale: isActive ? 1.4 : 1,
                            filter: isActive ? 'url(#glow-teal)' : 'none'
                          }}
                        />
                        <text
                          y={isRoot ? -24 : 5}
                          x={level % 2 === 0 ? 18 : -18}
                          textAnchor={level % 2 === 0 ? "start" : "end"}
                          className="text-[10px] font-mono fill-[#94A3B8] pointer-events-none"
                          style={{ fill: isActive ? '#fff' : undefined, fontWeight: isActive ? '900' : 'normal' }}
                        >
                          {node.char}
                        </text>
                      </g>
                      {node.dot && renderThemedNode(node.dot, x + xStep, y + yStep, level + 1, x, y)}
                      {node.dash && renderThemedNode(node.dash, x - xStep, y + yStep, level + 1, x, y)}
                    </React.Fragment>
                  );
                })(MORSE_TREE, 0, 40, 0, 0, 40)
              }
            </svg>
          </div>

          {/* Decoded Output Bar */}
          <div className="h-28 bg-[#0F172A] border border-[#2DD4BF]/30 rounded-2xl flex items-center justify-between px-6 md:px-10 gap-4 overflow-hidden shadow-[inset_0_0_30px_rgba(0,0,0,0.5)]">
            <div className="flex items-center gap-3">
              <Terminal className="w-5 h-5 text-[#2DD4BF]" />
              <div className="text-xl md:text-3xl font-mono font-bold text-white tracking-widest truncate max-w-[200px] md:max-w-md">
                {currentSequence || '....'}
              </div>
            </div>
            
            <div className="flex items-center gap-4">
              <div className="text-right">
                <div className="text-[10px] text-[#94A3B8] uppercase font-mono mb-1">State Decoded</div>
                <div className="bg-[#2DD4BF]/10 px-6 py-2 rounded-xl border border-[#2DD4BF]/20">
                  <span className="text-3xl font-black text-white font-mono">
                    {currentNode?.char && currentNode !== MORSE_TREE ? currentNode.char : '_'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Right Section: Library & Input */}
        <section className="flex-1 flex flex-col gap-6">
          <div className="flex-1 bg-[#111827] rounded-2xl border border-white/5 p-6 flex flex-col shrink-0">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xs font-bold text-[#94A3B8] uppercase tracking-widest">Transmitted Archive</h3>
              <div className="flex gap-2">
                <button onClick={backspaceMessage} className="p-2 hover:bg-white/5 rounded-lg transition-colors text-[#94A3B8]">
                  <Delete className="w-4 h-4" />
                </button>
                <button onClick={clearMessage} className="p-2 hover:bg-white/5 rounded-lg transition-colors text-[#94A3B8]">
                  <Hash className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="flex-1 min-h-[120px] bg-[#0A0A0B] rounded-xl border-l-2 border-[#2DD4BF] p-4 flex flex-col">
              <div className="text-2xl md:text-4xl font-mono font-black text-white break-all flex-1">
                {message || <span className="opacity-10">NULL_STREAM</span>}
                <motion.span 
                  animate={{ opacity: [1, 0.2] }} 
                  transition={{ duration: 0.5, repeat: Infinity, ease: 'steps(2)' }}
                  className="inline-block w-3 bg-[#2DD4BF] ml-1 h-[2rem] translate-y-1 shadow-[0_0_10px_#2DD4BF]"
                />
              </div>
              <div className="mt-4 flex justify-between items-center">
                <span className="text-[10px] text-[#2DD4BF] font-mono tracking-widest uppercase">Encryption: AES-256</span>
                <span className="text-[10px] text-[#94A3B8] font-mono">ID: {Math.random().toString(16).slice(2, 8).toUpperCase()}</span>
              </div>
            </div>

            <div className="mt-6 flex flex-col gap-2">
               <div className="flex justify-between items-center px-1">
                  <span className="text-[10px] text-[#94A3B8] font-mono uppercase">System Pulse</span>
                  <span className={`text-[10px] font-mono ${isPressing ? 'text-[#2DD4BF]' : 'text-slate-600'}`}>
                    {isPressing ? 'ACTIVE_TRANSMISSION' : 'WAITING_SYNC'}
                  </span>
               </div>
               <div className="h-1 bg-white/5 rounded-full overflow-hidden">
                  <motion.div 
                    animate={{ width: isPressing ? '100%' : '0%' }}
                    className="h-full bg-[#2DD4BF] shadow-[0_0_10px_#2DD4BF]"
                  />
               </div>
            </div>
          </div>

          {/* Pulse Pad Section */}
          <div className="h-64 bg-[#111827] rounded-3xl border border-[#2DD4BF]/10 p-8 flex flex-col items-center justify-center relative overflow-hidden group shadow-2xl">
            <div className="absolute top-4 left-6 font-mono text-[10px] text-[#2DD4BF]/40">INPUT TERMINAL // CONTACT_PAD_01</div>
            
            <motion.div 
              onMouseDown={handlePressStart}
              onMouseUp={handlePressEnd}
              onMouseLeave={() => isPressing && handlePressEnd()}
              onTouchStart={handlePressStart}
              onTouchEnd={handlePressEnd}
              whileTap={{ scale: 0.95 }}
              className={`w-44 h-44 rounded-full border-4 flex items-center justify-center relative transition-all duration-300 cursor-pointer shadow-[0_0_50px_rgba(45,212,191,0.05)] ${isPressing ? 'border-[#2DD4BF] scale-105' : 'border-[#2DD4BF]/20 hover:border-[#2DD4BF]/60'}`}
            >
              <div className="w-32 h-32 rounded-full bg-gradient-to-br from-[#1F2937] to-[#111827] border border-[#2DD4BF]/40 flex flex-col items-center justify-center text-center p-6 shadow-inner">
                <span className={`text-xs font-bold mb-2 uppercase tracking-widest transition-colors ${isPressing ? 'text-white' : 'text-[#2DD4BF]'}`}>Tap / Hold</span>
                <p className="text-[10px] text-[#94A3B8] leading-tight font-mono">. : SHORT<br/>- : LONG</p>
              </div>
              
              <div className={`absolute -bottom-4 px-3 py-1 text-[#0A0A0B] text-[10px] font-black uppercase tracking-widest transition-all ${isPressing ? 'bg-white scale-110' : 'bg-[#2DD4BF]'}`}>
                Manual Input
              </div>

              {/* Glowing ripple effect on press */}
              {isPressing && (
                <motion.div 
                  initial={{ scale: 0.8, opacity: 0.5 }}
                  animate={{ scale: 1.5, opacity: 0 }}
                  transition={{ duration: 1, repeat: Infinity }}
                  className="absolute inset-0 rounded-full border border-[#2DD4BF] pointer-events-none"
                />
              )}
            </motion.div>
          </div>
        </section>
      </main>

      {/* Footer Info */}
      <footer className="h-12 border-t border-white/5 px-6 md:px-10 flex items-center justify-between text-[10px] text-[#64748B] font-mono bg-[#0D0D0F] z-20">
        <span className="flex items-center gap-2">
          <div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" />
          SYSTEM STATUS: OPERATIONAL
        </span>
        <div className="hidden md:flex gap-6 uppercase">
          <span>Android Compose Wrapper: OK</span>
          <span>Latent Offset: 14ms</span>
        </div>
      </footer>
    </div>
  );
}
