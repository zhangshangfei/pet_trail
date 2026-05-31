(function() {
  'use strict';

  // ===== 导航栏滚动效果 =====
  var navbar = document.getElementById('navbar');
  var ticking = false;

  function updateNavbar() {
    if (window.pageYOffset > 60) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
    ticking = false;
  }

  window.addEventListener('scroll', function() {
    if (!ticking) {
      requestAnimationFrame(updateNavbar);
      ticking = true;
    }
  });

  // ===== 移动端菜单 =====
  var hamburger = document.getElementById('hamburger');
  var mobilePanel = document.getElementById('mobilePanel');

  if (hamburger && mobilePanel) {
    hamburger.addEventListener('click', function() {
      var isOpen = mobilePanel.classList.toggle('open');
      var spans = hamburger.querySelectorAll('span');
      if (isOpen) {
        spans[0].style.transform = 'rotate(45deg) translate(5px, 5px)';
        spans[1].style.opacity = '0';
        spans[2].style.transform = 'rotate(-45deg) translate(5px, -5px)';
      } else {
        spans[0].style.transform = '';
        spans[1].style.opacity = '';
        spans[2].style.transform = '';
      }
    });

    mobilePanel.querySelectorAll('a').forEach(function(link) {
      link.addEventListener('click', function() {
        mobilePanel.classList.remove('open');
        var spans = hamburger.querySelectorAll('span');
        spans[0].style.transform = '';
        spans[1].style.opacity = '';
        spans[2].style.transform = '';
      });
    });
  }

  // ===== 滚动动画（IntersectionObserver）=====
  function initScrollReveal() {
    var elements = document.querySelectorAll('.reveal-up');

    if (!elements.length) return;

    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting) {
          var delay = parseInt(entry.target.dataset.delay || 0, 10);
          setTimeout(function() {
            entry.target.classList.add('visible');
          }, delay * 100);
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -30px 0px' });

    elements.forEach(function(el) { observer.observe(el); });
  }

  // ===== 数字计数动画 =====
  function initCounters() {
    var counters = document.querySelectorAll('.stat-number[data-target]');

    if (!counters.length) return;

    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting) {
          var target = parseInt(entry.target.dataset.target, 10);
          if (isNaN(target)) return;
          animateNumber(entry.target, target, 1800);
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.5 });

    counters.forEach(function(c) { observer.observe(c); });
  }

  function animateNumber(el, target, duration) {
    var start = performance.now();

    function step(timestamp) {
      var progress = Math.min((timestamp - start) / duration, 1);
      var eased = 1 - Math.pow(1 - progress, 3);
      el.textContent = Math.floor(eased * target);
      if (progress < 1) {
        requestAnimationFrame(step);
      } else {
        el.textContent = target;
      }
    }

    requestAnimationFrame(step);
  }

  // ===== 平滑滚动锚点 =====
  document.querySelectorAll('a[href^="#"]').forEach(function(anchor) {
    anchor.addEventListener('click', function(e) {
      e.preventDefault();
      var targetId = this.getAttribute('href');
      if (targetId === '#') return;
      var targetEl = document.querySelector(targetId);
      if (!targetEl) return;
      var offset = navbar ? navbar.offsetHeight + 20 : 80;
      var top = targetEl.getBoundingClientRect().top + window.pageYOffset - offset;
      window.scrollTo({ top: top, behavior: 'smooth' });
    });
  });

  // ===== Bento 卡片光标跟随光效 =====
  function initCardGlow() {
    var cards = document.querySelectorAll('.bento-card');
    cards.forEach(function(card) {
      card.addEventListener('mousemove', function(e) {
        var rect = card.getBoundingClientRect();
        var x = e.clientX - rect.left;
        var y = e.clientY - rect.top;
        card.style.setProperty('--mouse-x', x + 'px');
        card.style.setProperty('--mouse-y', y + 'px');
      });
    });
  }

  // ===== 初始化 =====
  function init() {
    initScrollReveal();
    initCounters();
    initCardGlow();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
