(function() {
  'use strict';

  // ===== Navbar scroll effect =====
  const navbar = document.getElementById('navbar');
  let lastScroll = 0;
  let ticking = false;

  function updateNavbar() {
    const currentScroll = window.pageYOffset;
    if (currentScroll > 60) {
      navbar.classList.add('scrolled');
    } else {
      navbar.classList.remove('scrolled');
    }
    lastScroll = currentScroll;
    ticking = false;
  }

  window.addEventListener('scroll', function() {
    if (!ticking) {
      requestAnimationFrame(updateNavbar);
      ticking = true;
    }
  });

  // ===== Mobile menu toggle =====
  const mobileMenuBtn = document.getElementById('mobileMenuBtn');
  const mobileMenu = document.getElementById('mobileMenu');

  if (mobileMenuBtn && mobileMenu) {
    mobileMenuBtn.addEventListener('click', function() {
      mobileMenu.classList.toggle('open');
      const spans = mobileMenuBtn.querySelectorAll('span');
      if (mobileMenu.classList.contains('open')) {
        spans[0].style.transform = 'rotate(45deg) translate(5px, 5px)';
        spans[1].style.opacity = '0';
        spans[2].style.transform = 'rotate(-45deg) translate(5px, -5px)';
      } else {
        spans[0].style.transform = '';
        spans[1].style.opacity = '';
        spans[2].style.transform = '';
      }
    });

    mobileMenu.querySelectorAll('a').forEach(function(link) {
      link.addEventListener('click', function() {
        mobileMenu.classList.remove('open');
        const spans = mobileMenuBtn.querySelectorAll('span');
        spans[0].style.transform = '';
        spans[1].style.opacity = '';
        spans[2].style.transform = '';
      });
    });
  }

  // ===== Feature card spotlight effect =====
  function initSpotlightEffect() {
    var cards = document.querySelectorAll('.feature-card');
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

  // ===== Scroll animations with IntersectionObserver =====
  function initScrollAnimations() {
    var elements = document.querySelectorAll(
      '.feature-card, .highlight-item, .roadmap-phase, .premium-card, .value-card, .section-header'
    );
    elements.forEach(function(el) {
      el.classList.add('fade-in');
    });

    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting) {
          var delay = entry.target.dataset.delay || 0;
          setTimeout(function() {
            entry.target.classList.add('visible');
          }, delay * 100);
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1, rootMargin: '0px 0px -40px 0px' });

    elements.forEach(function(el) {
      observer.observe(el);
    });
  }

  // ===== Smooth scroll for anchor links =====
  document.querySelectorAll('a[href^="#"]').forEach(function(anchor) {
    anchor.addEventListener('click', function(e) {
      e.preventDefault();
      var target = document.querySelector(this.getAttribute('href'));
      if (target) {
        var offset = navbar ? navbar.offsetHeight + 20 : 80;
        var top = target.getBoundingClientRect().top + window.pageYOffset - offset;
        window.scrollTo({ top: top, behavior: 'smooth' });
      }
    });
  });

  // ===== Counter animation with easing =====
  function animateCounters() {
    var counters = document.querySelectorAll('.hero-stat-num');
    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting) {
          var text = entry.target.textContent;
          var num = parseInt(text);
          if (!isNaN(num) && num > 0) {
            var duration = 1800;
            var startTime = null;
            function step(timestamp) {
              if (!startTime) startTime = timestamp;
              var progress = Math.min((timestamp - startTime) / duration, 1);
              // Ease out cubic
              var eased = 1 - Math.pow(1 - progress, 3);
              entry.target.textContent = Math.floor(eased * num);
              if (progress < 1) {
                requestAnimationFrame(step);
              } else {
                entry.target.textContent = text;
              }
            }
            requestAnimationFrame(step);
          }
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.5 });

    counters.forEach(function(c) { observer.observe(c); });
  }

  // ===== Parallax effect for hero background =====
  function initParallax() {
    var heroGradient = document.querySelector('.hero-gradient');
    if (!heroGradient) return;

    var parallaxTicking = false;
    window.addEventListener('scroll', function() {
      if (!parallaxTicking) {
        requestAnimationFrame(function() {
          var scrolled = window.pageYOffset;
          heroGradient.style.transform = 'translateY(' + (scrolled * 0.15) + 'px)';
          parallaxTicking = false;
        });
        parallaxTicking = true;
      }
    });
  }

  // ===== Magnetic button effect =====
  function initMagneticButtons() {
    var buttons = document.querySelectorAll('.btn-primary, .nav-cta');
    buttons.forEach(function(btn) {
      btn.addEventListener('mousemove', function(e) {
        var rect = btn.getBoundingClientRect();
        var x = e.clientX - rect.left - rect.width / 2;
        var y = e.clientY - rect.top - rect.height / 2;
        btn.style.transform = 'translate(' + (x * 0.15) + 'px, ' + (y * 0.15) + 'px)';
      });

      btn.addEventListener('mouseleave', function() {
        btn.style.transform = '';
      });
    });
  }

  // ===== Text scramble effect for hero title accent =====
  function initTextScramble() {
    var accent = document.querySelector('.hero-title-accent');
    if (!accent) return;

    var originalText = accent.textContent;
    var chars = '!<>-_\\/[]{}—=+*^?#________';
    var frame = 0;
    var queue = [];
    var isAnimating = false;

    function update() {
      var output = '';
      var complete = 0;
      for (var i = 0; i < queue.length; i++) {
        var q = queue[i];
        if (q.from !== q.to) {
          if (q.from < q.to) q.from++;
          else q.from--;
          output += chars[Math.floor(Math.random() * chars.length)];
        } else {
          complete++;
          output += originalText[i];
        }
      }
      accent.textContent = output;
      if (complete === queue.length) {
        isAnimating = false;
      } else {
        requestAnimationFrame(update);
      }
    }

    accent.addEventListener('mouseenter', function() {
      if (isAnimating) return;
      isAnimating = true;
      queue = [];
      for (var i = 0; i < originalText.length; i++) {
        queue.push({ from: originalText.length, to: i });
      }
      update();
    });
  }

  // ===== Reveal on scroll for section headers =====
  function initSectionReveal() {
    var sections = document.querySelectorAll('section');
    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('section-visible');
        }
      });
    }, { threshold: 0.05 });

    sections.forEach(function(s) { observer.observe(s); });
  }

  // ===== Initialize everything =====
  function init() {
    initScrollAnimations();
    animateCounters();
    initSpotlightEffect();
    initParallax();
    initMagneticButtons();
    initTextScramble();
    initSectionReveal();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
