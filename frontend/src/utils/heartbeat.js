import { heartbeat } from '@openclaw/utils';

// 监控 heartbeat 功能
export const heartbeatMonitor = () => {
  const interval = setInterval(() => {
    heartbeat('heartbeat', 'heartbeat', 'heartbeat')
      .then(() => {
        console.log('Heartbeat sent successfully');
      })
      .catch((error) => {
        console.error('Heartbeat failed:', error);
      });
  }, 30000);

  return () => {
    clearInterval(interval);
  };
};

// 初始化 heartbeat
const heartbeat = heartbeat