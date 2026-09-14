import React from 'react';

export interface MenuBarItem<T extends string = string> {
  key: T;
  label: string;
  icon: React.ReactNode;
}

export interface MenuBarProps<T extends string = string> {
  active?: T;
  onSelect?: (key: T) => void;
  items?: MenuBarItem<T>[];
  docked?: boolean;
  className?: string;
}

const defaultIcons = {
  dashboard: (
    <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
      <path d="M3 9.5L12 4l9 5.5v7.5a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9.5z" />
      <path d="M9 22V12h6v10" />
    </svg>
  ),
  notifications: (
    <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
      <path d="M18 16v-5a6 6 0 1 0-12 0v5l-2 2v1h16v-1l-2-2z" />
      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
    </svg>
  ),
  settings: (
    <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h.09a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51h.09a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v.09a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
    </svg>
  ),
  help: (
    <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
      <circle cx="12" cy="12" r="10" />
      <path d="M9.09 9a3 3 0 1 1 5.82 1c0 2-3 3-3 3" />
      <circle cx="12" cy="17" r="1" />
    </svg>
  ),
  security: (
    <svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.5" viewBox="0 0 24 24">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
    </svg>
  ),
};

export interface IconButtonProps {
  icon: React.ReactNode;
  label: string;
  active?: boolean;
  onClick?: () => void;
  docked?: boolean;
}

export const IconButton: React.FC<IconButtonProps> = ({ icon, label, active, onClick, docked }) => {
  const [hovered, setHovered] = React.useState(false);
  const [showTooltip, setShowTooltip] = React.useState(false);
  const tooltipTimeout = React.useRef<ReturnType<typeof setTimeout> | null>(null);

  // Calculate width based on label length (min 44px for icon, plus label)
  const expandedWidth = Math.max(44 + label.length * 8.5 + 24, 110);

  const isExpanded = hovered || active;

  // Show tooltip on mobile tap if not expanded
  const handleMobileTooltip = (e: React.MouseEvent) => {
    if (window.innerWidth < 640 && !docked) {
      e.preventDefault();
      setShowTooltip(true);
      if (tooltipTimeout.current) clearTimeout(tooltipTimeout.current);
      tooltipTimeout.current = setTimeout(() => setShowTooltip(false), 1200);
    }
    if (onClick) onClick();
  };

  React.useEffect(() => () => {
    if (tooltipTimeout.current) clearTimeout(tooltipTimeout.current);
  }, []);

  return (
    <button
      type="button"
      aria-label={label}
      className={`flex items-center rounded-xl border transition-all duration-300 focus:outline-none relative overflow-visible select-none
        ${
          active
            ? 'border-[#E85002]/50 bg-[#1e2026] text-white font-semibold shadow-lg shadow-[#E85002]/10'
            : 'border-transparent text-[#A7A7A7] hover:text-[#F9F9F9] hover:bg-[#15171d]/60'
        }
        ${docked ? 'h-11 px-2.5 sm:px-3 justify-center' : 'h-11 px-0 sm:px-3.5 justify-center sm:justify-start'}
        bg-[#090a0d]
      `}
      style={{
        minWidth: 44,
        minHeight: 44,
        transition: 'background 0.25s, border 0.25s, width 0.35s cubic-bezier(0.4, 0, 0.2, 1)',
        paddingTop: 6,
        paddingBottom: 6,
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onClick={handleMobileTooltip}
    >
      {/* Tooltip for mobile view (when not docked) */}
      {!docked && (
        <span
          className={`sm:hidden absolute -top-8 left-1/2 -translate-x-1/2 bg-[#121212] text-white border border-white/10 text-xs rounded px-2 py-1 shadow-lg transition-opacity duration-200 pointer-events-none z-30
            ${showTooltip ? 'opacity-100' : 'opacity-0'}`}
        >
          {label}
        </span>
      )}

      <span className={`flex items-center justify-center w-8 h-8 transition-transform duration-200 ${active ? 'text-[#E85002] scale-105' : 'text-current'}`}>
        {icon}
      </span>

      <span
        className={`text-xs sm:text-sm font-medium transition-all duration-300 whitespace-nowrap pointer-events-none ml-1.5 overflow-hidden
          ${isExpanded ? 'opacity-100 max-w-[140px]' : 'opacity-0 max-w-0 ml-0'}`}
        style={{
          transition: 'opacity 0.25s, max-width 0.35s cubic-bezier(0.4,0,0.2,1), margin 0.25s',
          maxWidth: isExpanded ? expandedWidth - 44 : 0,
        }}
      >
        {label}
      </span>
    </button>
  );
};

export const MenuBar = <T extends string = string>({
  active = 'dashboard' as T,
  onSelect,
  items,
  docked = false,
  className = '',
}: MenuBarProps<T>) => {
  // If custom items are passed, use them; otherwise use default items
  const menuItems: MenuBarItem<T>[] = items || [
    { key: 'dashboard' as T, label: 'Dashboard', icon: defaultIcons.dashboard },
    { key: 'notifications' as T, label: 'Notifications', icon: defaultIcons.notifications },
    { key: 'settings' as T, label: 'Settings', icon: defaultIcons.settings },
    { key: 'help' as T, label: 'Help', icon: defaultIcons.help },
    { key: 'security' as T, label: 'Security', icon: defaultIcons.security },
  ];

  if (docked) {
    // Normal, non-floating bottom navigation bar
    return (
      <nav
        className={`w-full fixed bottom-0 left-0 right-0 h-16 bg-[#000000]/95 backdrop-blur-2xl border-t border-white/10 flex items-center justify-around sm:justify-center sm:gap-3 px-3 z-40 transition-all duration-300 ${className}`}
      >
        {menuItems.map((item) => (
          <IconButton
            key={item.key}
            icon={item.icon}
            label={item.label}
            active={active === item.key}
            onClick={() => onSelect?.(item.key)}
            docked={true}
          />
        ))}
      </nav>
    );
  }

  // Floating variant
  return (
    <nav
      className={`flex items-center gap-1.5 bg-[#090a0d] p-1.5 rounded-2xl border border-white/10 shadow-2xl w-fit mx-auto transition-all duration-300 ${className}`}
    >
      {menuItems.map((item, index) => (
        <React.Fragment key={item.key}>
          {index === 1 && <div className="w-px h-6 bg-white/10 mx-1" />}
          <IconButton
            icon={item.icon}
            label={item.label}
            active={active === item.key}
            onClick={() => onSelect?.(item.key)}
            docked={false}
          />
        </React.Fragment>
      ))}
    </nav>
  );
};
