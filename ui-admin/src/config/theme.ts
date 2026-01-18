import { type GlobalThemeOverrides } from 'naive-ui'

export const getGlassThemeOverrides = (isDark: boolean): GlobalThemeOverrides => {
  // 1. 核心品牌色与科技色
  const primaryColor = isDark ? '#00f2ff' : '#3b82f6'
  const primaryColorHover = isDark ? '#33f5ff' : '#60a5fa'
  const primaryColorPressed = isDark ? '#00d2ff' : '#2563eb'

  // 2. 玻璃材质参数
  const glassBg = isDark ? 'rgba(15, 23, 42, 0.75)' : 'rgba(255, 255, 255, 0.75)'
  const glassBorder = isDark ? 'rgba(0, 242, 255, 0.15)' : 'rgba(255, 255, 255, 0.5)'

  return {
    common: {
      primaryColor,
      primaryColorHover,
      primaryColorPressed,
      primaryColorSuppl: primaryColor,
      
      // 基础容器颜色
      bodyColor: isDark ? '#020617' : '#f8fafc',
      cardColor: glassBg,
      modalColor: glassBg,
      popoverColor: glassBg,
      tableColor: 'transparent',
      
      // 边框与分割线
      borderColor: glassBorder,
      dividerColor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)',
      
      // 圆角统一
      borderRadius: '14px',
      borderRadiusSmall: '8px',
      
      // 玻璃阴影控制：包含外部阴影和内部 1px 高光
      boxShadow1: isDark 
        ? '0 4px 12px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.1)' 
        : '0 4px 12px rgba(0, 0, 0, 0.05), inset 0 1px 0 rgba(255, 255, 255, 0.8)',
      boxShadow2: isDark 
        ? '0 12px 32px rgba(0, 0, 0, 0.6), 0 0 10px rgba(0, 242, 255, 0.1), inset 0 1px 0 rgba(255, 255, 255, 0.1)' 
        : '0 12px 32px rgba(31, 38, 135, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.8)',
    },
    Card: {
      borderRadius: '20px',
      borderColor: glassBorder,
      colorModal: glassBg,
      titleFontSizeMedium: '18px',
      titleFontWeight: '600',
    },
    Input: {
      color: isDark ? 'rgba(0, 0, 0, 0.2)' : 'rgba(255, 255, 255, 0.4)',
      colorFocus: isDark ? 'rgba(0, 0, 0, 0.3)' : 'rgba(255, 255, 255, 0.6)',
      border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)'}`,
      borderHover: `1px solid ${primaryColorHover}`,
      borderFocus: `1px solid ${primaryColor}`,
      boxShadowFocus: `0 0 8px ${isDark ? 'rgba(0, 242, 255, 0.2)' : 'rgba(59, 130, 246, 0.2)'}`,
      borderRadius: '10px',
    },
    Button: {
      borderRadiusMedium: '10px',
      fontWeight: '600',
      // 二级按钮也做玻璃处理
      colorSecondary: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)',
      colorSecondaryHover: isDark ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0, 0, 0, 0.08)',
    },
    Menu: {
      borderRadius: '12px',
      itemColorActive: isDark ? 'rgba(0, 242, 255, 0.1)' : 'rgba(59, 130, 246, 0.1)',
      itemColorActiveHover: isDark ? 'rgba(0, 242, 255, 0.15)' : 'rgba(59, 130, 246, 0.15)',
      itemTextColorActive: primaryColor,
      itemTextColorActiveHover: primaryColor,
      itemIconColorActive: primaryColor,
      itemIconColorActiveHover: primaryColor,
    },
    DataTable: {
      borderRadius: '16px',
      thColor: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.02)',
      tdColorHover: isDark ? 'rgba(255, 255, 255, 0.03)' : 'rgba(0, 0, 0, 0.01)',
      thFontWeight: 'bold',
    },
    InternalSelectMenu: {
      color: glassBg,
      borderRadius: '12px',
      optionColorHover: isDark ? 'rgba(0, 242, 255, 0.1)' : 'rgba(59, 130, 246, 0.05)',
    },
    Dialog: {
      borderRadius: '24px',
      color: glassBg,
    },
    Message: {
      borderRadius: '12px',
      color: glassBg,
    },
    Layout: {
      siderColor: 'transparent',
      headerColor: 'transparent',
      footerColor: 'transparent',
      color: 'transparent'
    },
    Tabs: {
      tabGapMediumCard: '8px',
      panePaddingMedium: '16px',
    }
  }
}
