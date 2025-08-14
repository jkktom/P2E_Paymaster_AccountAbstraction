'use client'

import { useState } from "react";
import Header from "@/components/Header";
import Sidebar from "@/components/Sidebar";

interface LayoutWrapperProps {
  children: React.ReactNode;
}

export default function LayoutWrapper({ children }: LayoutWrapperProps) {
  const [isMobileOpen, setIsMobileOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header isMobileOpen={isMobileOpen} setIsMobileOpen={setIsMobileOpen} />
      <div className="flex">
        <Sidebar isMobileOpen={isMobileOpen} setIsMobileOpen={setIsMobileOpen} />
        <main className="flex-1 lg:ml-0">
          {children}
        </main>
      </div>
      <footer className="bg-white border-t">
        <div className="max-w-7xl mx-auto px-4 py-6 text-center">
          <p className="text-sm">
            Technical Interview Submission - Blockchain Goods Exchange Service
          </p>
          <p className="text-xs mt-1">
            Spring Boot + zkSync + Account Abstraction + Next.js
          </p>
        </div>
      </footer>
    </div>
  );
}
