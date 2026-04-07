import { promises as fs } from "node:fs";
import path from "node:path";

const root = path.resolve(process.cwd(), "dist", "build", "mp-weixin");

async function exists(p) {
  try {
    await fs.access(p);
    return true;
  } catch {
    return false;
  }
}

async function copyDir(src, dest) {
  await fs.mkdir(dest, { recursive: true });
  const entries = await fs.readdir(src, { withFileTypes: true });
  await Promise.all(
    entries.map(async (e) => {
      const s = path.join(src, e.name);
      const d = path.join(dest, e.name);
      if (e.isDirectory()) return copyDir(s, d);
      await fs.copyFile(s, d);
    })
  );
}

async function main() {
  // 不再复制 pages/home 和 pages/me 到根目录
  // 所有页面都保持在 pages 目录下，避免路径问题
  const mapping = [
    // { src: path.join(root, "pages", "home"), dest: path.join(root, "home") },
    // { src: path.join(root, "pages", "me"), dest: path.join(root, "me") }
  ];

  for (const m of mapping) {
    if (await exists(m.src)) {
      await copyDir(m.src, m.dest);
    }
  }
}

await main();

