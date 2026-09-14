import React from 'react';
import { Home, Compass, Radio, Library, Settings } from 'lucide-react';
import { MenuBar, MenuBarItem } from './ui/animated-menu-bar';
import { TabType } from '../types';

interface BottomNavProps {
  currentTab: TabType;
  onTabChange: (tab: TabType) => void;
  className?: string;
}

const navItems: MenuBarItem<TabType>[] = [
  {
    key: 'home',
    label: 'Home',
    icon: <Home className="w-[18px] h-[18px]" />,
  },
  {
    key: 'search',
    label: 'Search',
    icon: <Compass className="w-[18px] h-[18px]" />,
  },
  {
    key: 'discover',
    label: 'Discover',
    icon: <Radio className="w-[18px] h-[18px]" />,
  },
  {
    key: 'library',
    label: 'Library',
    icon: <Library className="w-[18px] h-[18px]" />,
  },
  {
    key: 'settings',
    label: 'Settings',
    icon: <Settings className="w-[18px] h-[18px]" />,
  },
];

export const BottomNav: React.FC<BottomNavProps> = ({ currentTab, onTabChange, className = '' }) => {
  return (
    <div className={`md:hidden ${className}`}>
      <MenuBar<TabType>
        active={currentTab}
        onSelect={(tab) => onTabChange(tab)}
        items={navItems}
        docked={true}
      />
    </div>
  );
};
export default BottomNav;
